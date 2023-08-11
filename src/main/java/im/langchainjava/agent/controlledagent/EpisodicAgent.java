package im.langchainjava.agent.controlledagent;

import java.util.List;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.agent.controlledagent.model.TaskFailure;
import im.langchainjava.agent.functioncall.FunctionCallAgent;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.ControllorToolOut;
import im.langchainjava.tool.ControllorToolOut.Status;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.AgentToolOut.AgentToolOutStatus;
import im.langchainjava.tool.AgentToolOut.ControlSignal;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EpisodicAgent extends FunctionCallAgent{

    final EpisodicPromptProvider episodicPromptProvider;
    final EpisodeSolver solver;

    public static String CONTEXT_KEY_FORCE_BREAK = "control_force_break";

    public abstract void onWaitUserInput(String user);
    public abstract void onFinalAnswer(String user);
    public abstract void onFailedEpisode(String user);
    public abstract void onAssistantMessage(String user, String message);
    public abstract void onAssistantFunctionCall(String user, Tool tool, FunctionCall functionCall, AgentToolOut functionOut, boolean isUserTurn);
    public abstract void onAssistantFunctionCallError(String user, FunctionCall functionCall, Exception e, boolean isUserTurn);

    public EpisodicAgent(LlmService llm, EpisodicPromptProvider prompt, ChatMemoryProvider memory, CommandParser c) {
        super(llm, prompt, memory, c, prompt.getSolver());
        this.episodicPromptProvider = prompt;
        this.solver = prompt.getSolver();
    }

    @Override
    public boolean onInvokingAi(String user, boolean isUserTurn){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "The current episode was finished prematurely.");
        
        List<ChatMessage> prompt = episodicPromptProvider.getEpisodicControlPrompt(user);
        showMessages("episode", prompt);
        ChatMessage message = null;
        int retry = 3;
        while(true){
            try{
                message = getLlm().chatCompletion(user, 
                                                    prompt, 
                                                    this.episodicPromptProvider.getEpisodicControlFunctions(user), 
                                                    episodicPromptProvider.getEpisodicControlFunctionCall(user), 
                                                    this);
                break;
            }catch (Exception e){
                e.printStackTrace();
                if(--retry <= 0){
                    onAiException(user, e);
                    return false;
                }
            }
        }

        if(message == null || message.getFunctionCall() == null){
            return false;
        }
        
        log.info(JsonUtils.fromObject(message.getFunctionCall()));

        try{
            ToolOut out = task.getEpisodicControlFunction().invoke(user, message.getFunctionCall(), getMemoryProvider());
            if(out == null){
                onControllerException(user, "The controller function returns null");
                return false;
            }
            return doControl(user, out, task, isUserTurn); 
        }catch(Exception e){
            e.printStackTrace();
            onControllerException(user, "There is an exception while invoking controller function.\n" + e.getMessage());
            return false;
        }
    }

    private boolean doControl(String user, ToolOut output, Task task, boolean isUserTurn){
        Asserts.assertTrue(output instanceof ControllorToolOut, "The episodic controller does not return a controller tool out.");
        ControllorToolOut out = (ControllorToolOut) output;

        // wait for user input, the current task status is unchanged (neither success nor failed)
        if(out.getStatus() == Status.wait){
            onWaitUserInput(user);
            return false;
        }

        // halt immediatly
        if(out.getStatus() == Status.halt){
            onFailedEpisode(user);
            return false;
        }

        AgentToolOut toolOut = task.getToolOut();
        if(toolOut != null){
            
            if(toolOut.getStatus() == AgentToolOutStatus.control){
                if(toolOut.getControl() == ControlSignal.form){
                    onWaitUserInput(user);
                    return false;
                }
    
                if(toolOut.getControl() == ControlSignal.finish){
                    task.finish(user, toolOut.getOutput());
                    return next(user);
                }
            }

            if(toolOut.getStatus() != AgentToolOutStatus.success){
                task.fail(new TaskFailure(toolOut.getOutput()));
                return fail(user);
            }
        }


        // the current task is finished
        if(out.getStatus() == Status.success){
            // switch current task status to success;
            task.finish(out.getOutput());
            return success(user);
        }

        // The current task status is switched to failed;
        if(out.getStatus() == Status.failed){
            task.fail(new TaskFailure(out.getError()));
            return fail(user);
        }
        
        // The current task status is neither success or failed;
        if(out.getStatus() == Status.next){
            task.updateResult(out.getOutput());
            return next(user);
        }

        if(isUserTurn){
            // always run next round if the user has sent a message.
            return next(user);
        }

        throw new EpisodeException("The controller status " + out.getStatus().name() + " is not recognized.");
    }

    private boolean success(String user){
        // All tasks are finished
        Task nextTask = this.solver.solveCurrentTask(user);
        if(nextTask == null){
            onFinalAnswer(user);
            return false;
        }

        // resolved task is not null and not failed = there's more task in the episode
        return next(user);
    }

    private boolean fail(String user){
        // All tasks are finished
        Task nextTask = this.solver.solveCurrentTask(user);
        if(nextTask == null){
            onFinalAnswer(user);
            return false;
        }
        // If a pivot task is failed, fail the whole episode.
        if(nextTask.isFailed()){
            onFailedEpisode(user);
            return false;
        }

        // resolved task is not null and not failed = there is more task in the episode.
        return next(user);
    }

    private boolean next(String user){
        if(getContext(user, CONTEXT_KEY_FORCE_BREAK) != null && (Boolean) getContext(user, CONTEXT_KEY_FORCE_BREAK)){
            setContext(user, CONTEXT_KEY_FORCE_BREAK, Boolean.FALSE); 
            onWaitUserInput(user);
            return false;
        }
        return true;
    }
    
    private void onControllerException(String user, String message){
        log.info("Controller has an exception while processing messagge for user: " + user + "\n message: " + message);
    }

    public boolean onMessage(String user, String message, boolean isUserTurn){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Episode is finished prematurely.");
        Asserts.assertTrue(!task.isFailed(), "Task is failed unexpactivly.");
        task.addAssistMessage(user, message);
        //force break when assistant returns a text message.
        forceBreak(user, true);
        onAssistantMessage(user, message);
        return true;
    }

    @Override
    public boolean onFunctionCallResult(String user, Tool tool, FunctionCall functionCall, AgentToolOut functionOut, boolean isUserTurn){
        Asserts.assertTrue(functionOut != null, "Function Out must not be null.");
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Episode is finished prematurely.");
        Asserts.assertTrue(!task.isFailed(), "Task is failed unexpactivly.");
        
        if(functionOut.getStatus() == AgentToolOutStatus.control){
            task.addAssistMessage(user, functionOut.getOutput());
            onAssistantMessage(user, functionOut.getOutput());
        }else{
            task.addAssistFunctionCall(user, functionCall);
            task.addAssistMessage(user, functionOut.getOutput());
            onAssistantFunctionCall(user, tool, functionCall, functionOut, isUserTurn);
        }
        
        task.setToolOut(functionOut);

        return true;
    }
    
    public boolean onFunctionCallException(String user, FunctionCall call, Exception e, boolean isUserTurn){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Episode is finished prematurely.");
        Asserts.assertTrue(!task.isFailed(), "Task is failed unexpactivly.");

        task.addAssistFunctionCall(user, call);
        if(e != null && !StringUtil.isNullOrEmpty(e.getMessage())){
            task.addFunctionCallResult(user, e.getMessage());
        }else{
            task.addFunctionCallResult(user, "There is an exception while making function call " + call.getName());
        }
        
        onAssistantFunctionCallError(user, call, e, isUserTurn);
        return true;
    }

    public boolean onFunctionExecutionException(String user, Tool t, FunctionCall call, Exception e, boolean isUserTurn){
        AgentToolOut executionErrToolOut = null;
        if(e != null && !StringUtil.isNullOrEmpty(e.getMessage())){
            executionErrToolOut = ToolOuts.onToolError(user, e.getMessage());
        }else{
            executionErrToolOut = ToolOuts.onToolError(user, "There is an unknown exception while executing function call " + call.getName());
        }
        return onFunctionCallResult(user, t, call, executionErrToolOut, isUserTurn);
    }

    public void forceBreak(String user, boolean forceBreak){
        setContext(user, CONTEXT_KEY_FORCE_BREAK, forceBreak);
    }
}
