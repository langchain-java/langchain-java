package im.langchainjava.prompt;

import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;

public interface ChatPromptProvider {
    
    public List<ChatMessage> getPrompt(String user, boolean isUserTurn);

    public List<Function> getFunctions(String user);

    public FunctionCall getFunctionCall(String user);

    public static String grabChatString(List<ChatMessage> chats){
        StringBuilder sb = new StringBuilder();
        chats.forEach(msg -> grabRoleMessage(sb, msg.getRole(), msg.getContent()));
        return sb.toString();
    }

    static void grabRoleMessage(StringBuilder sb, String role, String message){
        sb.append(role).append(":").append("\t").append(message).append("\n");
    }
}
