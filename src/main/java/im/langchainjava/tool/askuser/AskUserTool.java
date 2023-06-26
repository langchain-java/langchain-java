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

public class AskUserTool extends BasicTool{

    private static String PARAM_QUERY="query";

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
                .description("A fully formed question to the user in Chinese, "
                        + " followed by `[提示: some options and examples of user's next input in Chinese]`. "
                        + " Sample: things to query the user in Chinese. [提示: examples of user's next input in Chinese].")
                .build();
        Map<String, FunctionProperty> properties = new HashMap<>();
        properties.put(PARAM_QUERY, fp);
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
        String message = call.getParsedArguments().get(PARAM_QUERY);
        wechat.sendMessageToUser(user, message);
        return waitUserInput(user);
    }


    
}
