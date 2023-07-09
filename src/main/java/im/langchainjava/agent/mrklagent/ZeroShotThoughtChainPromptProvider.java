package im.langchainjava.agent.mrklagent;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.BasicChatPromptProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            +"8. If the user's question is answered, you should farewell the user.\r\n"
            +"\"\"\"\r\n"
            +"Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous.\r\n";
    private static String DEF_STATEMENT = "Now let's work this out in a step by step way to be sure we have the right answer.\r\n";
    private static String DEF_PERSONALITY = "Your response should use the following format:\r\n"
                                            +"\"\"\"\r\n"
                                            +"Question: <user's question>\r\n"
                                            +"Thought: <think about what action to take next and how the action help in answering the question>\r\n"
                                            +"Message: <you message to the user>\r\n"
                                            +"Action: the next action to take. Should be one of {{message, function_call}}.\r\n"
                                            +"Function Name: <the function name to call>\r\n"
                                            +"Function Input: <the parameter of the function call>\r\n"
                                            +"\"\"\"";

    List<Tool> tools;

    List<Function> functions;
    String functionStr;

    String role;
    String insight;
    String statement;
    String personality;

    public ZeroShotThoughtChainPromptProvider role(String r){
        this.role = r;
        return this;
    }

    public ZeroShotThoughtChainPromptProvider insight(String insight){
        this.insight = insight;
        return this;
    }

    public ZeroShotThoughtChainPromptProvider statement(String statement){
        this.statement = statement;
        return this;
    }

    public ZeroShotThoughtChainPromptProvider personality(String personality){
        this.personality = personality;
        return this;
    }


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
        this.functions = new ArrayList<>();
        if(tools != null && !tools.isEmpty()){
            for(Tool t : tools){
                if(t.getFunction() == null){
                    continue;
                }
                this.functions.add(t.getFunction());
            }
        }
        this.functionStr = JsonUtils.fromList(functions);
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
        StringBuilder sb = new StringBuilder();
        sb.append("You may use the following functions by putting the function name and function input in your response:\r\n")
            .append("\"\"\"\r\n")
            .append(this.functionStr)
            .append("\r\n")
            .append("\"\"\"");

        String prompt = getRole() + getInsight() + sb.toString() + getStatement() + getPersonality();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        chats.addAll(super.getMemoryProvider().getPrompt(user));
        return chats;
    }

    public List<ChatMessage> getFunctionCallPrompt(String user, String message){
        List<ChatMessage> chats = new ArrayList<>();
        String prompt = "You are an action invoker. You should run the following steps based on the input text:\r\n"
                        + "Steps: \"\"\"\r\n"
                        + "1. If action is `message`, you should response with the `Message` from Input Text as raw text.\r\n"
                        + "2. If action is `function_call`, you should make a function call accordingly. \r\n"
                        +"\"\"\"\r\n"
                        +"Input Text: \"\"\"\r\n"
                        +message
                        +"\r\n\"\"\"";


        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        chats.add(sysMsg);
        return chats;
    }

}
