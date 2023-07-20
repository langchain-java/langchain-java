package im.langchainjava.tool;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionParameter;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.ControllorToolOut.Action;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;

public abstract class BasicTool implements Tool {

    public static String KEY_FUNC_OUT = "Function";
    public static String KEY_THOUGHT = "Thought";
    public static String KEY_CONTROL = "Control";


    public static String PARAMETER_TYPE_OBJECT = "object";

    public static String OBSERVATION_ON_EMPTY = "This function does not give any result this time.";
    public static String OBSERVATION_ON_ERR = "This function is not available. Don't use this function again.";
    public static String THOUGHT_ON_EMPTY = "There is no result. I should try another function or tell the user `我不知道`.";
    public static String THOUGHT_ON_ERR = "I should try another function or tell the user `我不知道`.";
    public static String THOUGHT = "Now I have the results. I should extract useful information from these results and inform the user.";
    public static String OBSERVATION_ON_INVALID_PARAM = "Invalid parameters: ";
    public static String THOUGT_ON_INVALID_PARAM = "I should ask the user to clarify the question or try the function again with corrected parameter.";

    String observationOnEmptyResult;
    String observationOnError;
    String observationOnInvalidParameter;
    String thought;
    String thoughtOnEmptyResult;
    String thoughtOnError;
    String thoughtOnInvalidInputFormat;
    String desc;
    Map<String, FunctionProperty> parameters;
    List<String> required;

    final public ChatMemoryProvider memoryProvider;
    // final Map<String,AsyncToolOut> users = new HashMap<>();

    public abstract String getName();
    public abstract String getDescription();
    public abstract Map<String, FunctionProperty> getProperties();
    public abstract List<String> getRequiredProperties();
    public abstract ToolOut doInvoke(String user, FunctionCall call);

    public BasicTool(ChatMemoryProvider memoryProvider){
        this.memoryProvider = memoryProvider;
        this.observationOnEmptyResult = null;
        this.observationOnError = null;
        this.observationOnInvalidParameter = null;
        this.thought = null;
        this.thoughtOnEmptyResult = null;
        this.thoughtOnError = null;
        this.thoughtOnInvalidInputFormat = null;
        this.desc = null;
        this.parameters = null;
        this.required = null;
    }

    // public class ToolCallback implements Function<TriggerInput, Void>{
    //     @Override
    //     public Void apply(TriggerInput input) {
    //         if(users.getOrDefault(input.getUser(), null)!=null){
    //             memoryProvider.onAssistantResponsed(input.getUser());
    //             memoryProvider.onAssistantResponsed(input.getUser());
    //             users.get(input.getUser()).applyLater(input);
    //             users.remove(input.getUser());
    //         }
    //         return null;
    //     }
    // } 

    public BasicTool description(String desc){
        this.desc = desc;
        return this;
    }

    public BasicTool inputFormat(Map<String, FunctionProperty> param){
        this.parameters = param;
        return this;
    }

    public BasicTool required(List<String> required){
        this.required =required;
        return this;
    }

    public BasicTool observationOnInvalidInputFormat(String obs){
        this.observationOnInvalidParameter = obs;
        return this;
    }

    public BasicTool observationOnEmptyResult(String obs){
        this.observationOnEmptyResult = obs;
        return this;
    }

    public BasicTool observationOnError(String obs){
        this.observationOnError = obs;
        return this;
    }

    public BasicTool thought(String th){
        this.thought = th;
        return this;
    }

    public BasicTool thoughtOnEmptyResult(String th){
        this.thoughtOnEmptyResult = th;
        return this;
    }


    public BasicTool thoughtOnError(String th){
        this.thoughtOnError = th;
        return this;
    }

    public BasicTool thoughtOnInvalidInputFormat(String th){
        this.thoughtOnInvalidInputFormat = th;
        return this;
    }

    public String getObservationOnEmptyResult(){
        return (this.observationOnEmptyResult != null) ? this.observationOnEmptyResult : OBSERVATION_ON_EMPTY; 
    }

    public String getObservationOnError(){
        return (this.observationOnError != null) ? this.observationOnError : OBSERVATION_ON_ERR; 
    }

    public String getObservationOnInvalidParameter(String message){
        return ((this.observationOnInvalidParameter != null) ? this.observationOnInvalidParameter : OBSERVATION_ON_INVALID_PARAM) + " " + message;
    }

    public String getThought(){
        return (this.thought != null) ? this.thought : THOUGHT;
    }

    public String getThoughtOnEmptyResult(){
        return (this.thoughtOnEmptyResult != null) ? this.thoughtOnEmptyResult : THOUGHT_ON_EMPTY;
    }

    public String getThoughtOnError(){
        return (this.thoughtOnError != null) ? this.thoughtOnError : THOUGHT_ON_ERR;
    }

    public String getThoughtOnInvalidParameter(){
        return (this.thoughtOnInvalidInputFormat != null) ? this.thoughtOnInvalidInputFormat : THOUGT_ON_INVALID_PARAM;
    }

    final public String getFunctionDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return getDescription();
    }

    final public Map<String, FunctionProperty> getFunctionProperties(){
        if(this.parameters != null){
            return this.parameters;
        }
        return getProperties();
    }

    final public List<String> getFunctionRequiredProperties(){
        if(this.required != null){
            return this.required;
        }
        return getRequiredProperties();
    }
    
    final public FunctionParameter getFunctionParameters() {
        return FunctionParameter.builder()
                .type(PARAMETER_TYPE_OBJECT)
                .properties(getFunctionProperties())
                .required(getFunctionRequiredProperties())
                .build();
    }

    public ToolOut invalidParameter(String user, String message){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, getObservationOnInvalidParameter(message))
                        .message(KEY_THOUGHT, getThoughtOnInvalidParameter())
                        .get();
    }

    // public ToolOut waitUserInput(String user){
    //     ToolOut out = ToolOuts.of(user, false)
    //                         .message(KEY_FUNC_OUT, "")
    //                         .sync();
    //     // this.users.put(user, out);
    //     return out;
    // }

    public ToolOut onResult(String user, String result){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, result)
                        .message(KEY_THOUGHT, getThought())
                        .get();
    }

    // public ToolOut onDisclosedResult(String user, String result, String disclosedResult){
    //     return ToolOuts.of(user, true)
    //                     .message(Tool.KEY_FUNC_OUT, result)
    //                     .message(Tool.KEY_THOUGHT, getThought())
    //                     .message(Tool.KEY_DISCLOSE, disclosedResult)
    //                     .sync();
    // }

    public ToolOut onToolError(String user){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, getObservationOnError())
                        .message(KEY_THOUGHT, getThoughtOnError())
                        .get();
    }

    public ToolOut onEmptyResult(String user){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, getObservationOnEmptyResult())
                        .message(KEY_THOUGHT, getThoughtOnEmptyResult())
                        .get();
    }

    public ToolOut onEmptyResult(String user, String message){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, message)
                        .message(KEY_THOUGHT, getThoughtOnEmptyResult())
                        .get();
    }

    public ToolOut endConversation(String user, String message){
        return ToolOuts.of(user, Action.endConversation)
                        .message(KEY_CONTROL, message)
                        .get();
    }

    public ToolOut next(String user, String message){
        return ToolOuts.of(user, Action.next)
                        .message(KEY_CONTROL, message)
                        .get();
    }

    public ToolOut waitUserInput(String user, String message){
        return ToolOuts.of(user, Action.waitUserInput)
                        .message(KEY_CONTROL, message)
                        .get();
    }

    // @Override
    // public void onClearedMemory(String user) {
    //     // this.users.remove(user);
    // }

    @Override
    final public im.langchainjava.llm.entity.function.Function getFunction(){
        return im.langchainjava.llm.entity.function.Function.builder()
                .name(getName())
                .description(getFunctionDescription())
                .parameters(getFunctionParameters())
                .build();
    }
    
    @Override
    final public im.langchainjava.llm.entity.function.FunctionCall getFunctionCall(){
        return im.langchainjava.llm.entity.function.FunctionCall.builder()
                .name(getName())
                .build();
    }

    @Override
    final public ToolOut invoke(String user, FunctionCall call){
        if(call == null ){
            throw new FunctionCallException("The function call is null!");
        }
        if(!call.getName().equals(getName())){
            throw new FunctionCallException("The function name does not match function call. Function name is "+ getName() + " while the function call is " + call.getName() + "." );
        }
        Map<String, JsonNode> params = parseFunctionCallParam(call);
        if(params == null){
            return invalidParameter(user, "Could not parse parameters for function " + call.getName() + ".");
        }
        call.setParsedArguments(params);

        List<String> requiredProperties = getFunctionRequiredProperties();
        if(requiredProperties != null && !requiredProperties.isEmpty() && call.getParsedArguments() != null){
            for(String r : requiredProperties){
                if(!call.getParsedArguments().containsKey(r)){
                    return invalidParameter(user, "Missing required parameter " + r + ".");
                }
                if(call.getParsedArguments().get(r).asText() == null){
                    return invalidParameter(user, "Required parameter " + r + " is null.");
                }
            }
        }

        return doInvoke(user, call);
    }

    private Map<String, JsonNode> parseFunctionCallParam(FunctionCall call){

        String rawArguments = call.getArguments();
        Map<String, JsonNode> params = null;
        if(!StringUtil.isNullOrEmpty(rawArguments)){
            params = JsonUtils.toMapOfJsonNode(rawArguments);
        }

        return params;
    }

}
