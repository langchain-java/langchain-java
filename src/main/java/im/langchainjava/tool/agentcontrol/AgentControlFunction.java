package im.langchainjava.tool.agentcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;

@Slf4j
public class AgentControlFunction extends BasicTool{

    public static String PARAM_SUMMARY = "summary";
    public static String PARAM_ASK = "ask_user";
    public static String PARAM_QUESTION = "user_requirement";
    public static String PARAM_EXP_1 = "example1";
    public static String PARAM_EXP_2 = "example2";
    public static String PARAM_EXP_3 = "example3";
    public static String PARAM_ACT = "action";
    public static String PARAM_ANSWERED = "fullfilled";
    public static String PARAM_ANSWERED_BY = "fullfilled_by";

    public static String ANSWERED_YES = "yes";
    public static String ANSWERED_NO = "no";
    public static String ANSWERED_PARTIAL = "partial";
    public static String ACTION_TRY_AGAIN = "try_another_function";
    public static String ACTION_END = "end_conversation";

    ImService im;

    public AgentControlFunction(ChatMemoryProvider memoryProvider, ImService im) {
        super(memoryProvider);
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
        FunctionProperty act = FunctionProperty.builder()
                .description(new StringBuilder()
                        .append("The action for the ai assistant to take. Must be one of {{wait_user_input, end_conversation, continue}}")
                        // .append("If the ai assistant is asking the user a question, this field should be `wait_user_input`.\r\n")
                        // .append("If the user's question is answered, this field should be `end_conversation`.\r\n")
                        // .append("If the ai assistant has made a function call and the function call failed, this field should be `continue`\r\n")
                        // .append("If user's requirement is partially fullfilled, this field should be `call_next_function`\r\n")
                        .toString())
                .build();
        properties.put(PARAM_ACT, act);
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
        required.add(PARAM_ACT);
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
        boolean answered = false;
        try{
            if(ToolUtils.compareStringParamIgnoreCase(call, PARAM_ANSWERED, ANSWERED_YES)){
                answered = true;
                log.info("Question is answered.");
            }
            if(answered || ToolUtils.compareStringParamIgnoreCase(call, PARAM_ACT, ACTION_END)){
                //should clear memory
                log.info("Should clear memory.");
                clear(user, call);
            }
        }catch(Exception e){
            //should not clear memory. do nothing.
            e.printStackTrace();
        }

        String ask = ToolUtils.getStringParam(call, PARAM_ASK);
        if(!StringUtil.isNullOrEmpty(ask)){
            im.sendMessageToUser(user, ask);
            return onResult(user, ask);
        }

        if(!answered){
            if(ToolUtils.compareStringParamIgnoreCase(call, PARAM_ANSWERED, ANSWERED_PARTIAL)){
                return onResult(user, null);
            }
            if(ToolUtils.compareStringParamIgnoreCase(call, PARAM_ACT, ACTION_TRY_AGAIN)){
                return onResult(user, null);
            }
        }
        if(ToolUtils.compareStringParamIgnoreCase(call, PARAM_ANSWERED, ANSWERED_NO)){
            im.sendMessageToUser(user, "[系统]\n如果对结果不满意，可以换个方式向我提问。");
        }

        return waitUserInput(user);
    }

    private void clear(String user, FunctionCall functionCall){

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
        
        String summary = ToolUtils.getStringParam(functionCall, PARAM_SUMMARY);
        String message = "";
        if(!StringUtil.isNullOrEmpty(summary)){
            message = summary + "\n\n";
        }
        message = message + "[系统]\n已经为您解答完毕，系统将不再保存之前的聊天记忆，让我们重新开始聊天吧。\n";

        if(!prompts.isEmpty()){
            message = message + "\n您可以这样问我：\n";
            for(String prompt : prompts){
                message = message + prompt + "\r\n";
            }
        }

        memoryProvider.reset(user);
        im.sendMessageToUser(user, message);
    }

}
