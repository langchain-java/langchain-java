package im.langchainjava.tool.askuser;

import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;

public class FarewellTool extends BasicTool{

    ImService im;

    List<Tool> tools;

    public FarewellTool(ChatMemoryProvider memoryProvider, ImService im, List<Tool> tools){
        super(memoryProvider);
        this.im = im;
        this.tools = tools;
    }

    @Override
    public String getToolName() {
        return "farewell";
    }

    @Override
    public String getDescription() {
        return " always use this tool if user's intention is to farewell. Never farewell the user if user's intention is not farewell. " ; 
    }

    @Override
    public String getInputFormat() {
        return " `Action Input` should be things to inform user followed by [提示: some examples of user's next input]. "
                + " Sample `Action Input`: things to inform user [提示: some examples of user's next input]";
    }

    private static String MSG = "感谢您的咨询，再见";

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String message = String.valueOf(action.getInput());
        if(action.getInput() == null || StringUtil.isNullOrEmpty(message)){
            message = MSG;
        }
        im.sendMessageToUser(user, message);
        clear(user);
        return endConversation(user, message);
    }

    private void clear(String user){
        memoryProvider.reset(user);
        for (Tool t : this.tools){
            t.onClearedMemory(user);
        }
        im.sendMessageToUser(user, "记忆已经清除，让我们重新开始聊天吧。");
    }


}