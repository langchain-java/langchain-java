package im.langchainjava.tool.askuser;

import im.langchainjava.im.ImService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.BasicTool;

public class AskUserTool extends BasicTool{

    ImService wechat;

    public AskUserTool(ChatMemoryProvider memoryProvider, ImService wechat){
        super(memoryProvider);
        this.wechat = wechat;
    }

    @Override
    public String getToolName() {
        return "ask_user";
    }

    @Override
    public String getDescription() {
        return "If you need to ask for clarification or extra information, you can ask the user questions with this ask_user tool. ";
    }


    @Override
    public String getInputFormat() {
        return "`Action Input` contains a full formed question to the user in Chinese, followed by `[提示: some options and examples of user's next input in Chinese]`. "
                + "Sample `Action Input`: things to inform user in Chinese. [提示: examples of user's next input in Chinese].";
    }


    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String message = String.valueOf(action.getInput());
        if(action.getInput() == null || message == null || message.isEmpty()){
            message = "请继续完善您的问题。";
        }
        wechat.sendMessageToUser(user, message);
        return waitUserInput(user);
    }


    
}
