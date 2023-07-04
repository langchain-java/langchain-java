package im.langchainjava.tool.askuser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;

public class FarewellTool extends BasicTool{

    public static String PARAM_MSG = "message";

    public static String PARAM_EXP = "example";

    ImService im;

    List<Tool> tools;

    public FarewellTool(ChatMemoryProvider memoryProvider, ImService im, List<Tool> tools){
        super(memoryProvider);
        this.im = im;
        this.tools = tools;
    }

    @Override
    public String getName() {
        return "farewell";
    }

    @Override
    public String getDescription() {
        return "Use this function to end a conversation with the user. " ; 
    }


    @Override
    public Map<String, FunctionProperty> getProperties() {
        FunctionProperty fp = FunctionProperty.builder()
                .description("A fully formed farewell message in Chinese to the user.")
                .build();
                Map<String, FunctionProperty> properties = new HashMap<>();
                properties.put(PARAM_MSG, fp);
        FunctionProperty fp2 = FunctionProperty.builder()
                .description("User's top 3 most possible input in Chinese.")
                .build();
        properties.put(PARAM_EXP, fp2);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_MSG);
        return required;
    }

    private static String MSG = "感谢您的咨询，再见";

    @Override
    public ToolOut doInvoke(String user, FunctionCall functionCall) {
        String message = functionCall.getParsedArguments().get(PARAM_MSG);
        if(StringUtil.isNullOrEmpty(message)){
            message = MSG;
        }

        String prompt = functionCall.getParsedArguments().get(PARAM_EXP);
        if(!StringUtil.isNullOrEmpty(prompt)){
            message = message + "\n" + prompt;
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