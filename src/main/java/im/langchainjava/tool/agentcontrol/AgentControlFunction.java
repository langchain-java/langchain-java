package im.langchainjava.tool.agentcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentControlFunction extends BasicTool{

    public static String PARAM_SUMMARY = "summary";
    public static String PARAM_ASK = "ask_user";
    public static String PARAM_QUESTION = "user_requirement";
    public static String PARAM_NUM_FUNC_CALL = "number_of_function_calls";
    public static String PARAM_ANSWER_ASSIS_KNOWLEDGE = "answer_with_assistant_knowledge";
    public static String PARAM_EXP_1 = "example1";
    public static String PARAM_EXP_2 = "example2";
    public static String PARAM_EXP_3 = "example3";
    // public static String PARAM_ACT = "action";
    public static String PARAM_ANSWERED = "fullfilled";
    public static String PARAM_ANSWERED_BY = "fullfilled_by";

    public static String ANSWERED_YES = "yes";
    public static String ANSWERED_NO = "no";
    public static String ANSWERED_PARTIAL = "partial";
    public static String ACTION_WAIT_USER = "wait_user_input";
    public static String ACTION_END = "end_conversation";

    ImService im;

    public AgentControlFunction(ImService im) {
        // super(memoryProvider);
        this.im = im;
    }

    @Override
    public String getName() {
        return "generate_prompt";
    }

    @Override
    public String getDescription() {
        return "This function is used to generate prompt for an ai assistant.";
    }


    @Override
    public Map<String, FunctionProperty> getProperties() {
        Map<String, FunctionProperty> properties = new HashMap<>();
        
        FunctionProperty numFuncCall = FunctionProperty.builder()
                .description("Number of function calls.")
                .build();
        properties.put(PARAM_NUM_FUNC_CALL, numFuncCall);

        FunctionProperty answerAssis = FunctionProperty.builder()
                .description("Did the assistant tried to answer with its own knowledge. Must be one of {{yes, no}}")
                .build();
        properties.put(PARAM_ANSWER_ASSIS_KNOWLEDGE, answerAssis);

        FunctionProperty question = FunctionProperty.builder()
                .description("The user's requirement.")
                .build();
        properties.put(PARAM_QUESTION, question);

        FunctionProperty ask = FunctionProperty.builder()
                .description("The question you want to ask the user.")
                .build();
        properties.put(PARAM_ASK, ask);

        FunctionProperty answeredBy = FunctionProperty.builder()
                .description("The requirement is fullfilled by ai assistant or function. Must be one of {{assistant, function}}")
                .build();
        properties.put(PARAM_ANSWERED_BY, answeredBy);
        FunctionProperty summary = FunctionProperty.builder()
                .description("Rewrite the answer into a short scentence.")
                .build();
        properties.put(PARAM_SUMMARY, summary);
        FunctionProperty answered = FunctionProperty.builder()
                .description("Check if the user's requirement is fullfilled. Must be one of {{yes, no, partial}}")
                .build();
        properties.put(PARAM_ANSWERED, answered);
        FunctionProperty exp1 = FunctionProperty.builder()
                .description("User's 1st most possible next input in Chinese.")
                .build();
        properties.put(PARAM_EXP_1, exp1);
        FunctionProperty exp2 = FunctionProperty.builder()
                .description("User's 2nd most possible next input in Chinese.")
                .build();
        properties.put(PARAM_EXP_2, exp2);
        FunctionProperty exp3 = FunctionProperty.builder()
                .description("User's 3rd most possible next input in Chinese.")
                .build();
        properties.put(PARAM_EXP_3, exp3);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_SUMMARY);
        required.add(PARAM_ASK); 
        required.add(PARAM_ANSWERED_BY);
        required.add(PARAM_ANSWER_ASSIS_KNOWLEDGE);
        required.add(PARAM_NUM_FUNC_CALL);
        required.add(PARAM_EXP_1);
        required.add(PARAM_EXP_2);
        required.add(PARAM_EXP_3);
        required.add(PARAM_ANSWERED);
        required.add(PARAM_QUESTION);
        return required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call) {
        log.info(JsonUtils.fromObject(call));
        
        
        // question is answered
        boolean answered = ToolUtils.compareStringParamIgnoreCase(call, PARAM_ANSWERED, ANSWERED_YES);
        if(answered){
            return finalAnswer(user, call);
        }
        
        // too many function call
        int funcNum = ToolUtils.getIntParam(call, PARAM_NUM_FUNC_CALL);
        boolean answerAssisKnowledge = ToolUtils.compareStringParamIgnoreCase(call, PARAM_ANSWER_ASSIS_KNOWLEDGE, ANSWERED_YES);
        if(funcNum >= 2 && answerAssisKnowledge){
            return endConversation(user, call);
        }

        // ask user a question
        String ask = ToolUtils.getStringParam(call, PARAM_ASK);
        if(!StringUtil.isNullOrEmpty(ask)){
            return askUser(user, call);
        }

        // continue
        return runAnotherRound(user, call);
    }

    private ToolOut runAnotherRound(String user, FunctionCall call){
        return next(user, null, null);
    }

    private ToolOut askUser(String user, FunctionCall functionCall){
        String ask = ToolUtils.getStringParam(functionCall, PARAM_ASK);
        return waitUserInput(user, null, ask);
    }

    private ToolOut finalAnswer(String user, FunctionCall functionCall){
        return endConversation(user, functionCall);
    }

    private ToolOut endConversation(String user, FunctionCall functionCall){

        String summary = ToolUtils.getStringParam(functionCall, PARAM_SUMMARY);
        String ask = ToolUtils.getStringParam(functionCall, PARAM_ASK);

        if(StringUtil.isNullOrEmpty(summary)){
            im.sendMessageToUser(user, summary);
        }
        if(StringUtil.isNullOrEmpty(ask)){
            im.sendMessageToUser(user, ask);
        }

        if(ToolUtils.compareStringParamIgnoreCase(functionCall, PARAM_ANSWERED, ANSWERED_NO)){
            im.sendMessageToUser(user, "[系统]\n如果对结果不满意，可以换个方式向我提问。");
        }

        String prompt = getPrompt(user, functionCall);
        if(StringUtil.isNullOrEmpty(prompt)){
            im.sendMessageToUser(user, prompt);
        }

        return endConversation(user, summary, ask);
    }

    private String getPrompt(String user, FunctionCall functionCall){

        List<String> prompts = new ArrayList<>();

        String prompt1 = ToolUtils.getStringParam(functionCall, PARAM_EXP_1);
        if(!StringUtil.isNullOrEmpty(prompt1)){
            prompts.add(prompt1);
        }

        String prompt2 = ToolUtils.getStringParam(functionCall, PARAM_EXP_2);
        if(!StringUtil.isNullOrEmpty(prompt2)){
            prompts.add(prompt2);
        }
        
        String prompt3 = ToolUtils.getStringParam(functionCall, PARAM_EXP_3);
        if(!StringUtil.isNullOrEmpty(prompt3)){
            prompts.add(prompt3);
        }
        
        StringBuilder sb = new StringBuilder("[提示]\n");

        if(!prompts.isEmpty()){
            sb.append("您可以这样问我：\n");
            for(String prompt : prompts){
                sb.append(prompt + "\r\n");
            }
        }

        return sb.toString();
    }

}
