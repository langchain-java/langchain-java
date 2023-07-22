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
import im.langchainjava.tool.Tool.FunctionMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ControlledAgent extends MemoryAgent{

    private static int MAX_ROUNDS = 20;

    ControllerChatPromptProvider controllerChatPromptProvider;

    // public abstract void onControllerReturned(String user, ControllorToolOut out);

    public abstract void onMaxRound(String user);

    public abstract void onMessage(String user, String message);

    public ControlledAgent(LlmService llm, ControllerChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c, tools);
        this.controllerChatPromptProvider = prompt;
    }

    @Override
    final public boolean onAssistantResponsed(String user, String content, int round) {
        
        if(content != null){
            onMessage(user, content);
        }

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
        
        try{
            ControllorToolOut out = (ControllorToolOut) controllerChatPromptProvider.getInvokableControllerFunction().invoke(user, message.getFunctionCall());
            if(out == null){
                onControllerException(user, "The controller function returns null");
                return false;
            }
            return doControl(user, out, round); 
        }catch(Exception e){
            e.printStackTrace();
            onControllerException(user, "There is an exception while invoking controller function.\n" + e.getMessage());
            return false;
        }
    }

    private boolean doControl(String user, ControllorToolOut out, int round){
        
        out.handlerForKey(BasicTool.KEY_CONTROL_SUMMARY, remember)
            .handlerForKey(BasicTool.KEY_CONTROL_ASK, remember)
            .run();

        if(out.getAction() == null){
            log.info("Controller action is null.");
            return false;
        }
        
        if(round >= MAX_ROUNDS){
            onMaxRound(user);
            endConversation(user);
            return false;
        }
        
        if(out.getAction() == Action.endConversation){
            endConversation(user);
            return false;
        }
        if(out.getAction() == Action.next){
            return true;
        }
        if(out.getAction() == Action.waitUserInput){
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
        rememberAssistantMessage(input.getUser(), input.getMessage() + " \n");
        return null;
    };

}
