package im.langchainjava.tool.askuser;

import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;

public class FarewellTool implements Tool{

    ImService im;

    ChatMemoryProvider memoryProvider;

    List<Tool> tools;

    String desc;

    public FarewellTool(ImService im, ChatMemoryProvider memoryProvider, List<Tool> tools){
        this.im = im;
        this.memoryProvider = memoryProvider;
        this.tools = tools;
        this.desc = null;
    }

    public FarewellTool(ImService im, ChatMemoryProvider memoryProvider, List<Tool> tools, String desc){
        this.im = im;
        this.memoryProvider = memoryProvider;
        this.tools = tools;
        this.desc = desc;
    }

    @Override
    public String getToolName() {
        return "farewell";
    }

    @Override
    public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return " always use this tool if user's intention is to farewell. Never farewell the user if user's intention is not farewell. " 
                + " Input should be things to inform user followed by [提示: some examples of user's next input]. "
                + " Sample input: `Action Input: things to inform user [提示: some examples of user's next input]`"; 
    }

    private static String MSG = "感谢您的咨询，再见";

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String message = String.valueOf(action.getInput());
        if(action.getInput() == null || StringUtil.isNullOrEmpty(message)){
            message = MSG;
        }
        clear(user);
        im.sendMessageToUser(user, message);
        return ToolOuts.of(user, false)
                        .message(Tool.KEY_OBSERVATION, message)
                        .sync();
    }

    @Override
    public void onClearedMemory(String user) {
    }
    
    private void clear(String user){
        memoryProvider.reset(user);
        for (Tool t : this.tools){
            t.onClearedMemory(user);
        }
        im.sendMessageToUser(user, "记忆已经清除，让我们重新开始聊天吧。");
    }
}