package im.langchainjava.agent.episode;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.episode.model.Task;
import im.langchainjava.agent.episode.model.TaskFailure;
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

    final EpisodicPromptProvider promptProvider;
    final EpisodeSolver solver;
    final LlmService llm;

    boolean showPrompt = false;

    public static String CONTEXT_KEY_FORCE_BREAK = "control_force_break";

    public abstract void onWaitUserInput(String user);
    public abstract void onFinalAnswer(String user);
    public abstract void onFailedEpisode(String user);
    public abstract void onAssistantMessage(String user, String message);
    public abstract void onAssistantFunctionCall(String user, Tool tool, FunctionCall functionCall, AgentToolOut functionOut, boolean isUserTurn);
    public abstract void onAssistantFunctionCallError(String user, FunctionCall functionCall, Exception e, boolean isUserTurn);

    public EpisodicAgent(LlmService llm, EpisodicPromptProvider prompt, ChatMemoryProvider memory, CommandParser c) {
        super(memory, c);
        this.promptProvider = prompt;
        this.solver = prompt.getSolver();
        this.llm = llm;
    }

    @Override
    public void onMessage(String user, String text){
        Task task = this.solver.getCurrentTask(user);
        Asserts.assertTrue(task != null, "The current episode was finished prematurely.");
        task.addUserMessage(user, text);
    }

    @Override
    public boolean onFunctionCall(String user, FunctionCall call, boolean isUserTurn, FunctionCall given){
        Task task = solver.getCurrentTask(user);

        if(given != null){
            Map<String, JsonNode> parsedParam = Tool.parseFunctionCallParam(call);
            call.setParsedArguments(parsedParam);
            return task.updateParams(call) == null;
        }

        solver.solveFunctionCall(user, call);
        return true;
    }

    @Override
    public boolean onChat(String user, boolean isUserTurn){
        Task task = this.solver.resolveCurrentTask(user);

        if(task == null){
            onFinalAnswer(user);
            return false;
        }

        if(task.getFunction() != null){
            if(task.getToolOut() == null){
                if(!task.isReady()){
                    onAgent(user, isUserTurn, task);
                }
                Task unresolvable = task.getOutstandingUnresolvableDependency();
                if(unresolvable != null && unresolvable.getFunction() != null){
                    this.solver.solveFunctionCall(user, unresolvable.getFunctionCall());
                    return true;
                }

                return handleFunctionCall(user, task.getFunction(), task.getFunctionCall(), isUserTurn);
            }
        }

        return onControl(user, isUserTurn, task);
    }

    private boolean onAgent(String user, boolean isUserTurn, Task task){

        ChatMessage chatMessage = null;

        if(showPrompt){
            showMessages("agent", promptProvider.getPrompt(user, isUserTurn));
        }
        FunctionCall call = promptProvider.getFunctionCall(user);
        chatMessage = llm.chatCompletion(user, promptProvider.getPrompt(user, isUserTurn), promptProvider.getFunctions(user), call, this);
        if(chatMessage == null){
            // all exceptions causing chatMessage == null are handled in chatCompletion. 
            // We simple do control logic here.
            return false;
        }
        
        return onAiResponse(user, chatMessage, isUserTurn, call);
    }

    public boolean onControl(String user, boolean isUserTurn, Task task){
        List<ChatMessage> prompt = promptProvider.getEpisodicControlPrompt(user);
        showMessages("episode", prompt);
        ChatMessage message = null;
        int retry = 3;
        while(true){
            try{
                message = this.llm.chatCompletion(user, 
                                                    prompt, 
                                                    this.promptProvider.getEpisodicControlFunctions(user), 
                                                    promptProvider.getEpisodicControlFunctionCall(user), 
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

        //update task result
        log.info(out.getOutput());
        task.updateResult(out.getOutput());

        // halt immediatly
        if(out.getStatus() == Status.halt){
            onFailedEpisode(user);
            return false;
        }

        // the current task is finished
        if(out.getStatus() == Status.success){
            // switch current task status to success;
            return finish(user, task);
        }

        // The current task status is switched to failed;
        if(out.getStatus() == Status.failed){
            return fail(user, task, new TaskFailure(out.getError()));
        }
        
        if(out.getStatus() == Status.next){

            //if the task is a function call task and the function call is a success, finish the function call task.
            if(task.getFunction() != null){
                Asserts.assertTrue(task.getToolOut() != null, "Function call task does not produce a tool out in the task.");
                if(task.getToolOut().getStatus() == AgentToolOutStatus.success){
                    return finish(user, task);
                }
            }


            if(next(user)){
                return onAgent(user, isUserTurn, task);
            }
            
            return false;
        }

        if(isUserTurn){
            // always run next round if the user has sent a message.
            return next(user);
        }

        throw new EpisodeException("The controller status " + out.getStatus().name() + " is not recognized.");
    }

    //insert the successor function call task to the parent task input.
    private void onTaskFinishedOrFailed(String user, Task task){
        if(task.getSuccessor() != null){
            solver.solveFunctionCall(user, task.getSuccessor().getFunctionCall());
        }
    }

    private boolean finish(String user, Task task){
        onTaskFinishedOrFailed(user, task);
        task.finish();
        Task nextTask = this.solver.resolveCurrentTask(user);
        // All tasks are finished
        if(nextTask == null){
            onFinalAnswer(user);
            return false;
        }
        // resolved task is not null and not failed = there's more task in the episode
        return next(user);
    }

    private boolean fail(String user, Task task, TaskFailure failure){
        onTaskFinishedOrFailed(user, task);
        task.fail(failure);
        // All tasks are finished
        Task nextTask = this.solver.resolveCurrentTask(user);
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

    public boolean onAgentMessage(String user, String message, boolean isUserTurn){
        Task task = this.solver.getCurrentTask(user);
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
        Task task = this.solver.getCurrentTask(user);
        Asserts.assertTrue(task != null, "Episode is finished prematurely.");
        Asserts.assertTrue(!task.isFailed(), "Task is failed unexpactivly.");
        
        task.setToolOut(functionOut);
        if(functionOut.getSuccessor() != null){
            task.setSuccessor(functionOut.getSuccessor());
        }

        if(functionOut.getStatus() == AgentToolOutStatus.control){
            task.addAssistMessage(user, functionOut.getOutput());
            onAssistantMessage(user, functionOut.getOutput());

            if(functionOut.getControl() == ControlSignal.form){
                // this.solver.popCurrentTask(user);
                task.finish();
                onWaitUserInput(user);
                return false;
            }

            if(functionOut.getControl() == ControlSignal.finish){
                task.finish();
                Task parent = task.getParent();
                Asserts.assertTrue(parent != null && !parent.isFailed(), "Parent task can not be null or failed for a control task.");
                return finish(user, parent);
            }

            if(functionOut.getControl() == ControlSignal.dispatch){
                task.setToolOut(null);
                Asserts.assertTrue(task.getToolOut().getDispatch() != null, "The dispatch tool out has no dispatched tool.");
                return onFunctionCall(user, task.getToolOut().getDispatch().getFunctionCall(), isUserTurn, null);
            }

            throw new EpisodeException("Control signal " + functionOut.getControl().name() + " is not recognized.");
        }

        task.addAssistFunctionCall(user, functionCall);
        task.addFunctionCallResult(user, functionOut.getOutput());
        if(functionOut.getStatus() == AgentToolOutStatus.error){
            return fail(user, task, new TaskFailure(functionOut.getOutput()));
        }

        if(functionOut.getStatus() == AgentToolOutStatus.success){
            return onControl(user, isUserTurn, task);
        }

        throw new EpisodeException("The agent tool out status " + functionOut.getStatus().name() + " is not recognized.");
    }
    
    @Override
    public boolean onFunctionCallException(String user, FunctionCall call, Exception e, boolean isUserTurn){
        Task task = this.solver.getCurrentTask(user);
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

    @Override
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
