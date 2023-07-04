package im.langchainjava.agent.mrklagent;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.BasicChatPromptProvider;
import im.langchainjava.tool.Tool;

public class ZeroShotThoughtChainPromptProvider extends BasicChatPromptProvider {
    private static String DEF_ROLE = "You are an ai assistant. ";
    private static String DEF_INSIGHT = 
            "Your task is to perform the following actions: \r\n"
            +"\"\"\"\r\n" 
            +"1. Extract the user's question from the chat messages between user and assitant. \r\n"
            +"2. Think about what is the best action to take. \r\n"
            +"3. If there are existing function outputs and those outputs are good enough to answer user's question, you should rewrite the function outputs into user friendly format and reply to the user.\r\n"
            +"4. You may ask the user to clarify his/her question or provide more information for the question.\r\n"
            +"5. You may make a function call from the function list to get information to the question.\r\n"
            +"6. If there are already more than 3 function calls and still no good results to the question. You should try to answer the question with your own knowledge.\r\n"
            +"7. If you don't know the answer, it is hornest to tell the user you don't know the answer.\r\n"
            +"\"\"\"\r\n"
            +"Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous.\r\n";
    private static String DEF_STATEMENT = "Now let's work this out in a step by step way to be sure we have the right answer.\r\n";
    private static String DEF_PERSONALITY = "Your response should be formated as a short paragraph including 3 to 5 scentences.\r\n";

    List<Tool> tools;

    String role;
    String insight;
    String statement;
    String personality;

    public String getRole(){
        if(this.role != null){
            return this.role;
        }
        return DEF_ROLE;
    }

    public String getInsight(){
        if(this.insight != null){
            return this.insight;
        }
        return DEF_INSIGHT;
    }

    public String getStatement(){
        if(this.statement != null){
            return this.statement;
        }
        return DEF_STATEMENT;
    }

    public String getPersonality(){
        if(this.personality != null){
            return this.personality;
        }
        return DEF_PERSONALITY;
    }

    public ZeroShotThoughtChainPromptProvider(ChatMemoryProvider memory, List<Tool> tools){
        super(memory);
        this.tools = tools; 
        this.role = null;
        this.insight = null;
        this.statement = null;
        this.personality = null;
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
        // String todayDate = "Today date is: " + new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + ".\n";
        String prompt = getRole() + getInsight() + getStatement() + getPersonality();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(super.getMemoryProvider().getPrompt(user));
        return chats;
    }

}
