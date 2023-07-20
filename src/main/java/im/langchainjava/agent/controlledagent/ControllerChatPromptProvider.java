package im.langchainjava.agent.controlledagent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.BasicChatPromptProvider;
import im.langchainjava.tool.agentcontrol.AgentControlFunction;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

public abstract class ControllerChatPromptProvider extends BasicChatPromptProvider{

    AgentControlFunction controllerFunction;

    public ControllerChatPromptProvider(ChatMemoryProvider memory, AgentControlFunction controllerFunction) {
        super(memory);
        this.controllerFunction = controllerFunction;
    }

    public List<ChatMessage> getControllerPrompt(String user){
        List<ChatMessage> chats = new ArrayList<>();
        String prompt = new StringBuilder()
                            .append("You are a conversation controller.\r\n")
                            .append("Your will be provided a conversation between the ai assistant and the user.\r\n")
                            .append("You observe the conversation and control the conversation with generate_prompt function.\r\n")
                            .append("Your task is to observe the conversation and perform the following steps:\"\"\"\r\n")
                            .append("1. Extract user's requirement from the conversation.\r\n")
                            .append("2. Check if the requirement is fullfilled.\r\n")
                            .append("3. If the requirement is fullfilled, put `end_conversation` to action field of the function call\r\n")
                            .append("4. If the requirement is not fullfilled or partially fullfilled, or the assistant should try to make another function call, put `continue` to action field.\r\n\"\"\"")
                            .append("5. If the requirement is not fullfilled after 2 function calls and the ai assistant don't know the final answer, put `end_conversation` to action field and put your own answer to the summary field.")
                            .append("Don't make assumptions about what values to plug into the generate_prompt function. You should leave the field blank if you don't know what value to put.\r\n")
                            .toString();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(getChatHistory(user));
        return chats;
    }

    AgentControlFunction getInvokableControllerFunction(){
        return this.controllerFunction;
    }

    List<Function> getControllerFunctions(){
        return Collections.singletonList(this.controllerFunction.getFunction());
    }

    FunctionCall getControllerFunctionCall(){
        return controllerFunction.getFunctionCall();
    }

}
