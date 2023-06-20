package im.langchainjava.tool.askchatbot;

import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.BasicTool;
import lombok.Setter;

public class AskChatbotTool extends BasicTool{

    LlmService llm;

    ImService imService;

    @Setter
    String prompt;

    public AskChatbotTool(ChatMemoryProvider memoryProvider, LlmService llmService, ImService im){
        super(memoryProvider);
        this.llm = llmService;
        this.imService = im;
        this.prompt = null;
    }

    @Override
    public String getToolName() {
        return "search_chatbot"; 
    }

    @Override
    public String getDescription() {
        return "Only use this tool if you could not find good answer from the internet."
        + " Don't use this tool if you can not find user's intention. "
        + " Don't use this tool when you need to ask the user for more information."
        + " Don't use this tool if you find user is insulting.";
    }
    
    @Override
    public String getInputFormat() {
        return "`Action Input` should be a full formed question to ask."; 
    }

    private static String sys = "Answer the question below and reply in Chinese. If you don't have the answer, you may say `我不知道`."
        + "Input always starts with `Action Input:`. ";
    
    private String getPrompt(){
        if(this.prompt != null){
            return prompt;
        }
        return sys;
    }
 
    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String input = String.valueOf(action.getInput());
        this.imService.sendMessageToUser(user, "[系统]\n正在搜索: " + input);
        List<ChatMessage> prompt = new ArrayList<>();
        prompt.add(new ChatMessage("system", getPrompt()));
        prompt.add(new ChatMessage("user", input));
        String resp = this.llm.chatCompletion(user, prompt);
        return onResult(user, resp);
    }
}
