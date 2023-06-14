package im.langchainjava.prompt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.ChatResponseParser;
import lombok.Getter;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

@Getter
public class BasicChatPromptProvider implements ChatPromptProvider {

    ChatResponseParser<?> parser;

    ChatMemoryProvider memoryProvider;

    public BasicChatPromptProvider(ChatResponseParser<?> parser, ChatMemoryProvider memory){
        this.parser = parser;
        this.memoryProvider = memory;
    }

    public String getPrefix(){
        return "You are AI assistant.";
    }

    public String getSubfix(){
        return "Now answer the following question:\n";
    }

    @Override
    public List<ChatMessage> getPrompt(String user) {
        List<ChatMessage> chats = new ArrayList<>();
        String todayDate = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
        String prompt = getPrefix() + todayDate + parser.getStructurePrompt() + getSubfix();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(this.memoryProvider.getPrompt(user));
        return chats;
    }
}
