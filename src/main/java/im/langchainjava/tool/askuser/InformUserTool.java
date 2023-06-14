package im.langchainjava.tool.askuser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.agent.AsyncAgent.TriggerInput;
import im.langchainjava.agent.mrklagent.OneRoundMrklAgent;
import im.langchainjava.im.ImService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.Tool;

public class InformUserTool implements Tool{

    ImService wechat;

    Map<String,AsyncToolOut> users;

    ChatMemoryProvider memoryProvider;

    String desc;
    
    String defaultMsg;

    public class GreetingToolCallback implements Function<TriggerInput, Void>{
        @Override
        public Void apply(TriggerInput input) {
            if(users.getOrDefault(input.getUser(), null)!=null){
                memoryProvider.drainPendingMessages(input.getUser());
                users.get(input.getUser()).applyLater(input);
                users.remove(input.getUser());
            }
            return null;
        }
    } 
    
    public void registerTool(OneRoundMrklAgent agent){
        agent.registerTrigger(new GreetingToolCallback());
    }

    public InformUserTool(ImService wechat, ChatMemoryProvider memoryProvider){
        this.wechat = wechat;
        this.memoryProvider = memoryProvider;
        this.users = new HashMap<>();
        this.desc = null;
        this.defaultMsg = null;
    }

    public InformUserTool(ImService wechat, ChatMemoryProvider memoryProvider, String desc, String defaultMsg){
        this.wechat = wechat;
        this.memoryProvider = memoryProvider;
        this.users = new HashMap<>();
        this.desc = desc;
        this.defaultMsg = defaultMsg;
    }


    @Override
    public String getToolName() {
        return "inform_user";
    }

    @Override
    public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return " never use this tool to search for answers to the question. "
            + " Use this tool to reply to the user's message. "
            + " If the user's intention is greeting, you should greet back. "
            + " Whatever the user said, you should always reply politly in Chinese in the action input. "
            + " Input always starts with `Action Input:`. Input contains your message to the user in Chinese, followed by `[提示: some examples of user's next input in Chinese]`. "
            + " Sample input: `Action Input: things to inform user in Chinese. [提示: examples of user's next input in Chinese]`.";
    }

    private static String DEF_MSG = "请问您有什么问题需要咨询吗;)?"; 

    private String getDefaultMessage(){
        if(this.defaultMsg != null){
            return this.defaultMsg;
        }
        return DEF_MSG;
    }

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String message = getDefaultMessage();
        String actMsg = String.valueOf(action.getInput()).trim();
        if(action.getInput() != null && !"none".equals(actMsg) && !"无".equals(actMsg)){
            message = actMsg;
        }else{
            message = getDefaultMessage();
        }
        wechat.sendMessageToUser(user, message);
        AsyncToolOut out = ToolOuts.of(user, false).message(Tool.KEY_OBSERVATION,"").async();
        this.users.put(user, out);
        return out;
    }

    
    @Override
    public void onClearedMemory(String user) {
        this.users.remove(user);
    }
    
}
