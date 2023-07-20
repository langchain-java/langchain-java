package im.langchainjava.agent.controlledagent;

import java.util.List;

import im.langchainjava.agent.FunctionCallAgent;
import im.langchainjava.agent.MemoryAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;

public abstract class ControlledAgent extends FunctionCallAgent{

    ControllerChatPromptProvider controllerChatPromptProvider;
    public ControlledAgent(LlmService llm, ControllerChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c, tools);
        this.controllerChatPromptProvider = prompt;
    }

    // @Override
    // public boolean onChat(String user) {
    //     List<ChatMessage> prompt = controllerChatPromptProvider.getControllerPrompt(user);
    //     ChatMessage message = getLlm().chatCompletion(user, prompt, controllerChatPromptProvider.getControllerFunctions(), controllerChatPromptProvider.getControllerFunctionCall(), null);
    //     if(message.getFunctionCall() != null){
    //         ToolOut out = controllerChatPromptProvider.getInvokableControllerFunction().invoke(user, message.getFunctionCall());
    //         return out.apply(null);
    //     }
    //     return true;
    // }
    
}
