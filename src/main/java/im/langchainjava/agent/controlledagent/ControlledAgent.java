package im.langchainjava.agent.controlledagent;

import java.util.List;
import java.util.function.Function;

import im.langchainjava.agent.MemoryAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ControllorToolOut;
import im.langchainjava.tool.ControllorToolOut.Action;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut.FunctionMessage;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ControlledAgent extends MemoryAgent{

    public static String CONTEXT_KEY_FORCE_END_CONVERSATION = "control_force_end";
    public static String CONTEXT_KEY_FORCE_WAIT_USER = "control_force_wait";

    ControllerChatPromptProvider controllerChatPromptProvider;

    public abstract void onPartialAnswer(String user);
    public abstract void onMessage(String user, String message);
    public abstract void onWaitUserInput(String user);
    public abstract void onFinalAnswer(String user);

    public ControlledAgent(LlmService llm, ControllerChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c, tools);
        this.controllerChatPromptProvider = prompt;
    }

    @Override
    public boolean onAssistantInvoke(String user){
        List<ChatMessage> prompt = controllerChatPromptProvider.getControllerPrompt(user);
        ChatMessage message = null;
        int retry = 3;
        while(true){
            try{
                message = getLlm().chatCompletion(user, prompt, controllerChatPromptProvider.getControllerFunctions(), controllerChatPromptProvider.getControllerFunctionCall(), this);
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
            ControllorToolOut out = (ControllorToolOut) controllerChatPromptProvider.getInvokableControllerFunction().invoke(user, message.getFunctionCall(), getMemoryProvider());
            if(out == null){
                onControllerException(user, "The controller function returns null");
                return false;
            }
            boolean controlled = doControl(user, out); 
            setContext(user, CONTEXT_KEY_FORCE_WAIT_USER, Boolean.FALSE);
            return controlled;
        }catch(Exception e){
            e.printStackTrace();
            onControllerException(user, "There is an exception while invoking controller function.\n" + e.getMessage());
            return false;
        }
    }


    private boolean doControl(String user, ControllorToolOut out){
        
        out
            .handlerForKey(BasicTool.KEY_CONTROL_SUMMARY, remember)
            .handlerForKey(BasicTool.KEY_CONTROL_ASK, remember)
            .handlerForKey(BasicTool.KEY_THOUGHT, remember)
            .run();

        if(out.getAction() == null){
            log.info("Controller action is null.");
            return false;
        }

        if(out.getAction() == Action.endConversation){
            onPartialAnswer(user);
            endConversation(user);
            return false;
        }
        if(out.getAction() == Action.next){
            if(getContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION) != null && ((Boolean)getContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION))){
                onPartialAnswer(user);
                endConversation(user);
                return false;
            }
            if(getContext(user, CONTEXT_KEY_FORCE_WAIT_USER) != null && ((Boolean)getContext(user, CONTEXT_KEY_FORCE_WAIT_USER))){
                onWaitUserInput(user);
                return false;
            }
            return true;
        }
        if(out.getAction() == Action.waitUserInput){
            onWaitUserInput(user);
            return false;
        }
        if(out.getAction() == Action.finalAnswer){
            onFinalAnswer(user);
            endConversation(user);
            return false;
        }
        return false;
    }

    
    public void onControllerException(String user, String message){
        log.info("Controller has an exception while processing messagge for user: " + user + "\n message: " + message);
        return;
    }

    // think
    final private Function<FunctionMessage, Void> remember = input -> {
        if(StringUtil.isNullOrEmpty(input.getMessage())){
            return null;
        }
        rememberAssistantMessage(input.getUser(), input.getMessage() + " \n");
        return null;
    };


    @Override
    final public void onAssistantResponsed(String user, String content, boolean isAssistantMessage) {
        if(content != null){
            onMessage(user, content);
            setContext(user, CONTEXT_KEY_FORCE_WAIT_USER, Boolean.TRUE);
        }
    }

    @Override
    public void onAiException(String user, Exception e){
        setContext(user, CONTEXT_KEY_FORCE_WAIT_USER, Boolean.FALSE);
    }

    @Override
    public void onMaxTokenExceeded(String user) {
        endConversation(user);
    }
    
    @Override
    public void onMaxRound(String user){
        setContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION, Boolean.TRUE);
    }

    @Override
    public void onMaxFunctionCall(String user){
        setContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION, Boolean.TRUE);
    }

    @Override
    public void onAgentEndConversation(String user){
        setContext(user, CONTEXT_KEY_FORCE_END_CONVERSATION, Boolean.TRUE);
    }

}
