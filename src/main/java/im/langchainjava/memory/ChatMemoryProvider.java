package im.langchainjava.memory;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public interface ChatMemoryProvider {
    List<ChatMessage> getPrompt(String user);

    void onReceiveUserMessage(String user, String message);

    void onReceiveSystemMessage(String user, String message);

    void onReceiveAssisMessage(String user, String message);

    void setContextForUser(String user, String key, Object value);

    Object getContextForUser(String user, String key, Object defaultValue);

    void clearHistory(String user);

    int countUserMessage(String user);

    void addEndingMessage(String user, String message);

    void clearEndingMessage(String user);

    void onAssistantResponsed(String user, String message);

    void showMemory(String user);

    List<ChatMessage> getPendingMessage(String user);

    void drainPendingMessages(String user);

    void reset(String user);

    int getRound(String user);

    int incrRoundAndGet(String user);
}
