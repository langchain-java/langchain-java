package im.langchainjava.prompt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import lombok.Getter;

import static im.langchainjava.llm.LlmService.ROLE_SYSTEM;

@Getter
public class BasicChatPromptProvider implements ChatPromptProvider {

    ChatMemoryProvider memoryProvider;

    public BasicChatPromptProvider(ChatMemoryProvider memory){
        this.memoryProvider = memory;
    }

    private String getPrefix(){
        return "You are AI assistant.";
    }

    private String getSubfix(){
        return "Now answer the following question:\n";
    }

    public List<ChatMessage> getChatHistory(String user){
        return this.memoryProvider.getPrompt(user);
    }

    @Override
    public List<ChatMessage> getPrompt(String user) {
        List<ChatMessage> chats = new ArrayList<>();
        String todayDate = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
        String prompt = getPrefix() + todayDate + getSubfix();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(this.memoryProvider.getPrompt(user));
        return chats;
    }

    @Override
    public List<Function> getFunctions(String user) {
        return null;
    }

    @Override
    public FunctionCall getFunctionCall(String user) {
        return null;
    }

}
