package im.langchainjava.tool.askchatbot;

import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.Tool;

public class AskChatbotTool implements Tool{

    LlmService llm;

    ImService imService;

    String desc;

    String prompt;

    public AskChatbotTool(LlmService llmService, ImService im){
        this.llm = llmService;
        this.imService = im;
        this.desc = null;
        this.prompt = null;
    }

    public AskChatbotTool(LlmService llmService, ImService im, String desc, String prompt){
        this.llm = llmService;
        this.imService = im;
        this.desc = desc;
        this.prompt = prompt;
    }

    @Override
    public String getToolName() {
        return "search_chatbot"; 
    }

    @Override
    public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return "Only use this tool if you could not find good answer from the internet."
        + " Don't use this tool if you can not find user's intention. "
        + " Don't use this tool when you need to ask the user for more information."
        + " Don't use this tool if you find user is insulting. "
        + " Input should be the question to ask. "; 
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
        return ToolOuts
                .of(user, true)
                .message(Tool.KEY_OBSERVATION, resp)
                .message(Tool.KEY_THOUGHT, "Now I have result from chatbot, I need to inform user with the result. ")
                .sync();
    }

    
    @Override
    public void onClearedMemory(String user) {
    }
    
}
