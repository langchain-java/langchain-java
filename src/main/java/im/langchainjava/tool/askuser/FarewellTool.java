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

    public static String PARAM_EXP_1 = "example1";
    public static String PARAM_EXP_2 = "example2";
    public static String PARAM_EXP_3 = "example3";


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
        FunctionProperty exp1 = FunctionProperty.builder()
                .description("User's 1st most possible next question in Chinese.")
                .build();
        properties.put(PARAM_EXP_1, exp1);
        FunctionProperty exp2 = FunctionProperty.builder()
                .description("User's 2nd most possible next question in Chinese.")
                .build();
        properties.put(PARAM_EXP_2, exp2);
        FunctionProperty exp3 = FunctionProperty.builder()
                .description("User's 3rd most possible next question in Chinese.")
                .build();
        properties.put(PARAM_EXP_3, exp3);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_MSG);
        required.add(PARAM_EXP_1);
        required.add(PARAM_EXP_2);
        required.add(PARAM_EXP_3);
        return required;
    }

    private static String MSG = "感谢您的咨询，再见";

    @Override
    public ToolOut doInvoke(String user, FunctionCall functionCall) {
        String message = functionCall.getParsedArguments().get(PARAM_MSG).asText();
        if(StringUtil.isNullOrEmpty(message)){
            message = MSG;
        }
        List<String> prompts = new ArrayList<>();

        String prompt1 = functionCall.getParsedArguments().get(PARAM_EXP_1).asText();
        if(!StringUtil.isNullOrEmpty(prompt1)){
            prompts.add(prompt1);
        }

        String prompt2 = functionCall.getParsedArguments().get(PARAM_EXP_2).asText();
        if(!StringUtil.isNullOrEmpty(prompt2)){
            prompts.add(prompt2);
        }
        
        String prompt3 = functionCall.getParsedArguments().get(PARAM_EXP_3).asText();
        if(!StringUtil.isNullOrEmpty(prompt3)){
            prompts.add(prompt3);
        }
        
        if(!prompts.isEmpty()){
            message = message + "\n您可以试下这样问我：\n";
            for(String prompt : prompts){
                message = message + prompt + "\r\n";
            }
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