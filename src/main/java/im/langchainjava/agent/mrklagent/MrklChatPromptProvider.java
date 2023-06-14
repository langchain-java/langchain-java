package im.langchainjava.agent.mrklagent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.ChatResponseParser;
import im.langchainjava.prompt.BasicChatPromptProvider;
import im.langchainjava.tool.Tool;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

public class MrklChatPromptProvider extends BasicChatPromptProvider {

    private static String internalPrefix = " Answer the following questions as best you can. "
            + "You should interact with the user's until you have a specific question about travelling. Then use the online tools or use your own knowledge to get an answer. "
            + "You have access to the following tools:\n";

    List<Tool> tools;

    String prefix;

    public MrklChatPromptProvider(ChatResponseParser<?> parser, ChatMemoryProvider memory, List<Tool> tools){
        super(parser, memory);
        this.tools = tools; 
        this.prefix = null;
    }

    public MrklChatPromptProvider(ChatResponseParser<?> parser, ChatMemoryProvider memory, List<Tool> tools, String prefix){
        super(parser, memory);
        this.tools = tools;
        this.prefix = prefix; 
    }

    public String getPrefix(){
        String pre = "You are a chatbot assistant. ";
        if(this.prefix != null){
            pre = this.prefix;
        }
        return pre + internalPrefix;
    }

    private String iDontKnowStr(){
        return "If there is not suitable tool to answer the question, you should produce your own answer. If the tool does not provide reasonable answer, you should always choose to ask the user to clarify the question or produce your own answer. If you don't know the answer, It is hornest to say 'I don't know' as the final answer.\n";
    }

    public String getSubfix(){
        return "Now detect the user's attention and fullfill the intention:";
    }

    @Override 
    public List<ChatMessage> getPrompt(String user) {
        List<ChatMessage> chats = new ArrayList<>();
        List<String> toolNames = new ArrayList<>();
        List<String> toolDesc = new ArrayList<>();
        for(Tool t : this.tools){
            toolNames.add(t.getToolName());
            toolDesc.add(t.getToolName() + ": \t" + t.getToolDescription());
        }
        String toolStr = String.join(",", toolNames) + ".\n";
        String toolDescStr = "Tool Usage:\n" + String.join("\n", toolDesc) + "\n";
        String todayDate = "Today date is: " + new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + ".\n";
        String prompt = todayDate + getPrefix() + toolStr + toolDescStr + iDontKnowStr() +  super.getParser().getStructurePrompt() + "\n" + getSubfix();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(super.getMemoryProvider().getPrompt(user));

        return chats;
    }

}
