package im.langchainjava.prompt;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public interface ChatPromptProvider {
    
    public List<ChatMessage> getPrompt(String user);

    public static String grabChatString(List<ChatMessage> chats){
        StringBuilder sb = new StringBuilder();
        chats.forEach(msg -> grabRoleMessage(sb, msg.getRole(), msg.getContent()));
        return sb.toString();
    }

    static void grabRoleMessage(StringBuilder sb, String role, String message){
        sb.append(role).append(":").append("\t").append(message).append("\n");
    }
}
