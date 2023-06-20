package im.langchainjava.tool.askuser;

import im.langchainjava.im.ImService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.BasicTool;

public class InformUserTool extends BasicTool{

    private static String DEF_MSG = "请问您有什么问题需要咨询吗;)?"; 

    ImService wechat;

    String defaultMsg;

    public InformUserTool(ChatMemoryProvider memoryProvider, ImService wechat){
        super(memoryProvider);
        this.wechat = wechat;
        this.defaultMsg = null;
    }

    public InformUserTool defaultMessage(String message){
        this.defaultMsg = message;
        return this;
    }

    @Override
    public String getToolName() {
        return "inform_user";
    }

    @Override
    public String getDescription() {
        return " never use this tool to search for answers to the question. "
            + " Use this tool to reply to the user's message. "
            + " If the user's intention is greeting, you should greet back. "
            + " Whatever the user said, you should always reply politly in Chinese in the action input. ";
    }


    @Override
    public String getInputFormat() {
        return "`Action Input` contains your message to the user in Chinese, followed by `[提示: some examples of user's next input in Chinese]`. "
            + " Example: `Action Input: things to inform user in Chinese. [提示: examples of user's next input in Chinese]`.";
    }

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
        return waitUserInput(user);
    }

}
