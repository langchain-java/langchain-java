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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AskUserTool implements Tool{

    ImService wechat;

    Map<String,AsyncToolOut> users;

    ChatMemoryProvider memoryProvider;

    String desc;

    public class ToolCallback implements Function<TriggerInput, Void>{
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

    public AskUserTool(ImService wechat, ChatMemoryProvider memoryProvider){
        this.wechat = wechat;
        this.memoryProvider = memoryProvider;
        this.users = new HashMap<>();
        this.desc = null;
    }

    public AskUserTool(ImService wechat, ChatMemoryProvider memoryProvider, String desc){
        this.wechat = wechat;
        this.memoryProvider = memoryProvider;
        this.users = new HashMap<>();
        this.desc = desc;
    }

    public void registerTool(OneRoundMrklAgent agent){
        agent.registerTrigger(new ToolCallback());
    }

    @Override
    public String getToolName() {
        return "ask_user";
    }

    @Override
    public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return "If you need to ask for clarification or extra information, you can ask the user questions with this ask_user tool. "
        + "If you can not extract a travel related question from user, you can ask the user to ask questions related to travel with this ask_user tool. "
        + "Input always starts with `Action Input:`. "
        + "Input contains a full formed question to the user in Chinese, followed by `[提示: some options and examples of user's next input in Chinese]`. "
        + "Sample input: `Action Input: things to inform user in Chinese. [提示: examples of user's next input in Chinese]`.";
    }

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String message = String.valueOf(action.getInput());
        if(action.getInput() == null || message == null || message.isEmpty()){
            message = "请继续完善您的问题。";
        }
        wechat.sendMessageToUser(user, message);
        AsyncToolOut out = ToolOuts.of(user, false)
                                .message(Tool.KEY_OBSERVATION, "")
                                .async();
        this.users.put(user, out);
        return out;
    }

    @Override
    public void onClearedMemory(String user) {
        this.users.remove(user);
    }
    
}
