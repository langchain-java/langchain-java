package im.langchainjava.agent.controlledagent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import im.langchainjava.agent.CommandAgent;
import im.langchainjava.agent.MemoryAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.controlledagent.model.Episode;
import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.agent.controlledagent.model.TaskFailure;
import im.langchainjava.agent.functioncall.FunctionCallAgent;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ControllorToolOut;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ControllorToolOut.Status;
import im.langchainjava.tool.ToolOut.FunctionMessage;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EpisodicAgent extends FunctionCallAgent{

    final EpisodicPromptProvider episodicPromptProvider;
    final EpisodeSolver solver;

    public abstract void onPartialAnswer(String user);
    public abstract void onMessage(String user, String message);
    public abstract void onWaitUserInput(String user);
    public abstract void onFinalAnswer(String user);

    public EpisodicAgent(LlmService llm, EpisodicPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Task> tools) {
        super(llm, prompt, memory, c, prompt.getSolver());
        this.episodicPromptProvider = prompt;
        this.solver = prompt.getSolver();
    }

    @Override
    public boolean onInvokingAi(String user, boolean isUserTurn){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "The current episode was finished prematurely.");
        
        List<ChatMessage> prompt = episodicPromptProvider.getEpisodicControlPrompt(user);
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
            boolean controlled = doControl(user, out); 
            // setContext(user, CONTEXT_KEY_FORCE_WAIT_USER, Boolean.FALSE);
            return controlled;
        }catch(Exception e){
            e.printStackTrace();
            onControllerException(user, "There is an exception while invoking controller function.\n" + e.getMessage());
            return false;
        }
    }

    private boolean doControl(String user, ToolOut output){
        Asserts.assertTrue(output instanceof ControllorToolOut, "The episodic controller does not return a controller tool out.");
        ControllorToolOut out = (ControllorToolOut) output;
        
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "The current episode was finished prematurely.");

        // halt immediatly
        if(out.getStatus() == Status.halt){
            onHalt(user);
            return false;
        }

        // the current task is finished
        if(out.getStatus() == Status.success){
            //TODO: finish must has a result
            task.finish();
        }

        if(out.getStatus() == Status.failed){
            task.fail(new TaskFailure());
        }

        if(out.getStatus() != Status.next){
            task = this.solver.solveCurrentTask(user);
            if(task == null){
                onFinalAnswer(user);
                return false;
            }
            if(task.isFailed()){
                onFailed(user);
                return false;
            }
        }
        return true;
    }
    
    private void onControllerException(String user, String message){
        log.info("Controller has an exception while processing messagge for user: " + user + "\n message: " + message);
        return;
    }



    // @Override
    // public void onAiException(String user, Exception e){
    //     setContext(user, CONTEXT_KEY_FORCE_WAIT_USER, Boolean.FALSE);
    // }

    // @Override
    // public void onMaxTokenExceeded(String user) {
    //     endConversation(user);
    // }
    
    // @Override
    // public void onMaxRound(String user){
    //     setContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION, Boolean.TRUE);
    // }

    // @Override
    // public void onMaxFunctionCall(String user){
    //     setContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION, Boolean.TRUE);
    // }

    // @Override
    // public void onAgentEndConversation(String user){
    //     setContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION, Boolean.TRUE);
    // }

}
