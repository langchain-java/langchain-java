package im.langchainjava.llm;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;


public interface LlmService {
    public String chatCompletion(String user, List<ChatMessage> messages);
}
