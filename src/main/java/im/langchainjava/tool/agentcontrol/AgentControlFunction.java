package im.langchainjava.tool.agentcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.BasicChatMemory;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentControlFunction extends BasicTool{

    public static String CONTEXT_KEY_CITY = "city";

    // public static String PARAM_SUMMARY = "summary";
    // public static String PARAM_ASK = "ask_user";
    // public static String PARAM_MSG = "message";
    public static String PARAM_WAIT_USER = "wait_user_input";
    public static String PARAM_PLAN = "action_plan";
    public static String PARAM_TO_DO = "best_action";
    public static String PARAM_CONTINUE = "continue";
    public static String PARAM_QUESTION = "user_requirement";
    // public static String PARAM_CITY = "city";
    public static String PARAM_EXP_1 = "example1";
    public static String PARAM_EXP_2 = "example2";
    public static String PARAM_EXP_3 = "example3";
    public static String PARAM_ANSWERED = "fullfilled";
    // public static String PARAM_ANSWERED_BY = "fullfilled_by";

    public static String ANSWERED_YES = "yes";
    public static String ANSWERED_NO = "no";
    public static String ANSWERED_PARTIAL = "partial";
    public static String ACTION_WAIT_USER = "wait_user_input";
    public static String ACTION_END = "end_conversation";

    // public static int MAX_FUNC_CALL = 5;

    ImService im;
    ChatMemoryProvider memory;

    public AgentControlFunction(ImService im, ChatMemoryProvider memory) {
        this.memory = memory;
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
        
        FunctionProperty question = FunctionProperty.builder()
                .description("Rewrite the user's requirement into a fully formed scentece.")
                .build();
        properties.put(PARAM_QUESTION, question);
        
        // FunctionProperty msg = FunctionProperty.builder()
        //         .description("Your question or message for the user.")
        //         .build();
        // properties.put(PARAM_MSG, msg);

        FunctionProperty wait = FunctionProperty.builder()
                .description("Is the assistant waiting user's input for a question. Must be one of {{yes, no}}.")
                .build();
        properties.put(PARAM_WAIT_USER, wait);


        FunctionProperty todo = FunctionProperty.builder()
                .description("Think how to work out the user's requirement in a step by step way, and choose the right action to do. Must be one of {{inform_user, ask_user, get_current_weather, get_weather_forecast, get_recommendations_from_web}}")
                .build();
        properties.put(PARAM_TO_DO, todo);

        FunctionProperty next = FunctionProperty.builder()
                .description("whether the assistant should continue working on the user's requirement. Must be one of {{yes, no}}")
                .build();
        properties.put(PARAM_CONTINUE, next);

        FunctionProperty answered = FunctionProperty.builder()
                .description("Check if the user's requirement is fullfilled. Must be one of {{yes, no}}")
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
        // required.add(PARAM_SUMMARY);
        required.add(PARAM_CONTINUE);
        // required.add(PARAM_MSG);
        required.add(PARAM_WAIT_USER); 
        // required.add(PARAM_PLAN);
        required.add(PARAM_TO_DO);
        // required.add(PARAM_ANSWERED_BY);
        // required.add(PARAM_ANSWER_ASSIS_KNOWLEDGE);
        // required.add(PARAM_NUM_FUNC_CALL);
        required.add(PARAM_EXP_1);
        required.add(PARAM_EXP_2);
        required.add(PARAM_EXP_3);
        required.add(PARAM_ANSWERED);
        required.add(PARAM_QUESTION);
        return required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        
        // question is answered
        if(ToolUtils.compareStringParamIgnoreCase(call, PARAM_ANSWERED, ANSWERED_YES)){
            return endConversation(user, call, true);
        }
                    
        if(isUserTurn(user, memory)){
            return runAnotherRound(user, call, null);
        }

        if(ToolUtils.compareStringParamIgnoreCase(call, PARAM_WAIT_USER, ANSWERED_YES)){
            return waitUserInput(user, null, null, null);
        }

        if(!ToolUtils.compareStringParamIgnoreCase(call, PARAM_CONTINUE, ANSWERED_NO)){
            return runAnotherRound(user, call, null);
        }

        // continue
        String todo = ToolUtils.getStringParam(call, PARAM_TO_DO);
        if(StringUtil.isNullOrEmpty(todo)){
            todo = ToolUtils.getStringParam(call, PARAM_PLAN);
        }
        if(StringUtil.isNullOrEmpty(todo)){
            // String msg = ToolUtils.getStringParam(call, PARAM_MSG);


            // if(!StringUtil.isNullOrEmpty(msg)){
            //     // return runAnotherRound(user, call, "Thought: I should leave this message to user: \"\"\""+ msg+ "\"\"\"");
            //     return runAnotherRound(user, call, null);
            // }



            return waitUserInput(user, null, null, null);
        }else{
            // todo = "Thought: I should  \r\n\"\"\"\r\n" + todo + "\r\n\"\"\"";
            return runAnotherRound(user, call, null);
        }
    }

    private ToolOut runAnotherRound(String user, FunctionCall call, String thought){
        return next(user, thought, null, null);
    }

    private ToolOut endConversation(String user, FunctionCall functionCall, boolean finalAnswer){

        // String summary = ToolUtils.getStringParam(functionCall, PARAM_SUMMARY);
        // String ask = ToolUtils.getStringParam(functionCall, PARAM_ASK);

        // if(!StringUtil.isNullOrEmpty(summary)){
        //     im.sendMessageToUser(user, summary);
        // }
        // if(!StringUtil.isNullOrEmpty(ask)){
        //     im.sendMessageToUser(user, ask);
        // }

        // String prompt = getPrompt(user, functionCall);
        // if(StringUtil.isNullOrEmpty(prompt)){
        //     im.sendMessageToUser(user, prompt);
        // }

        if(finalAnswer){
            log.info("final answer");
            return finalAnswer(user, null, null);
            // return finalAnswer(user, summary, ask);
        }else{
            log.info("end conversation");
            return endConversation(user, null, null);
            // return endConversation(user, summary, ask);
        }
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

    public boolean isUserTurn(String user, ChatMemoryProvider memory){
        List<ChatMessage> pending = memory.getPendingMessage(user);
        if(pending.isEmpty()){
            return false;
        }
        return pending.get(0).getRole().equals(BasicChatMemory.ROLE_USER);
    }


}
