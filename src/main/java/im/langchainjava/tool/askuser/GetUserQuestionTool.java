package im.langchainjava.tool.askuser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.utils.StringUtil;

public class GetUserQuestionTool extends Tool{
    private static String EXTRACT = "the_user_s_question_about_travelling";
    private static String EXTRACT_DESC = "the user's question about travelling";
    private static String PARAM_QUESTION = "the_question_to_ask_user";
    private static String PARAM_QUESTION_DESC = "a fully formed question to ask the user";

    ImService im;

    public GetUserQuestionTool(ImService im){
        super(false);
        this.im = im;
        extractionName(EXTRACT);
        extraction(EXTRACT_DESC);
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        String question = ToolUtils.getStringParam(call, PARAM_QUESTION);
        if(StringUtil.isNullOrEmpty(question)){
            return ToolOuts.invalidParameter(user, "there is no question to ask.");
        }
        im.sendMessageToUser(user, question);
        return ToolOuts.onAskUser(user, question);
    }

    @Override
    public String getName() {
        return "ask_user_a_question";
    }

    @Override
    public String getDescription() {
        return "use this function to ask the user a question";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        FunctionProperty p = FunctionProperty.builder().description(PARAM_QUESTION_DESC).build();
        Map<String, FunctionProperty> properties = new HashMap<>();
        properties.put(PARAM_QUESTION, p);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_QUESTION);
        return required;
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        return null;
    }
   
}
