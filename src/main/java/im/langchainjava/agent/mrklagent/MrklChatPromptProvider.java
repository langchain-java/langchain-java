package im.langchainjava.agent.mrklagent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.BasicChatPromptProvider;
import im.langchainjava.tool.Tool;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

public class MrklChatPromptProvider extends BasicChatPromptProvider {

    private static String internalPrefix = 
            "You will take the following routine to serve the user: \n"
            +"1. Extract the user's intention. \n"
            +"2. Think about what action to take. You may call a function in the function list or reploy with your own knowledge. \n"
            +"3. Call the function and get the function output. \n"
            +"4. Extract the function output and infrom user. \n"
            +"Now answer the following questions with your own knowledge or using the functions. "
            +"Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous.";

    List<Tool> tools;

    String prefix;

    public MrklChatPromptProvider(ChatMemoryProvider memory, List<Tool> tools){
        super(memory);
        this.tools = tools; 
        this.prefix = null;
    }

    public MrklChatPromptProvider(ChatMemoryProvider memory, List<Tool> tools, String prefix){
        super(memory);
        this.tools = tools;
        this.prefix = prefix; 
    }

    public String getPrefix(){
        String pre = "You are a chatbot assistant that serves to fullfill user's requests. ";
        if(this.prefix != null){
            pre = this.prefix;
        }
        return pre + internalPrefix;
    }

    private String iDontKnowStr(){
        return "If there is not suitable function for the user's request, you should use your own knowledge to answer the question. If the functions does not provide reasonable answer, you should always choose to ask the user to clarify the question or produce your own answer. If you don't know the answer, It is hornest to say 'I don't know' as the final answer.\n";
    }

    public String getSubfix(){
        return "Now detect the user's intention and fullfill the intention:";
    }

    @Override
    public List<Function> getFunctions(String user) {
        List<Function> funs = new ArrayList<>();
        for(Tool t : this.tools){
            funs.add(t.getFunction());
        }
        return funs;
    }

    @Override 
    public List<ChatMessage> getPrompt(String user) {
        List<ChatMessage> chats = new ArrayList<>();
        String todayDate = "Today date is: " + new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + ".\n";
        String prompt = todayDate + getPrefix() + iDontKnowStr() +  "\n" + getSubfix();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(super.getMemoryProvider().getPrompt(user));
        return chats;
    }

}
