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
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;

public class AskUserTool extends BasicTool{

    private static String PARAM_QUERY="query";

    public static String PARAM_EXP = "example";

    ImService wechat;

    public AskUserTool(ChatMemoryProvider memoryProvider, ImService wechat){
        super(memoryProvider);
        this.wechat = wechat;
    }

    @Override
    public String getName() {
        return "ask_user";
    }

    @Override
    public String getDescription() {
        return "If you need to ask for clarification or extra information, you can ask the user questions with this ask_user function. ";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        FunctionProperty fp = FunctionProperty.builder()
                .description("A fully formed question to the user in Chinese.")
                .build();
        Map<String, FunctionProperty> properties = new HashMap<>();
        properties.put(PARAM_QUERY, fp);
        FunctionProperty fp2 = FunctionProperty.builder()
                .description("User's top 3 most possible input in Chinese.")
                .build();
        properties.put(PARAM_EXP, fp2);

        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_QUERY);
        return required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call) {
        String message = call.getParsedArguments().get(PARAM_QUERY).asText();

        String prompt = call.getParsedArguments().get(PARAM_EXP).asText();
        if(!StringUtil.isNullOrEmpty(prompt)){
            message = message + "\n" + prompt;
        }
        wechat.sendMessageToUser(user, message);
        return waitUserInput(user);
    }


    
}
