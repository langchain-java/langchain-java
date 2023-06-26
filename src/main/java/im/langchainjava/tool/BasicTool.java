package im.langchainjava.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.agent.AsyncAgent.TriggerInput;
import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.agent.mrklagent.OneRoundMrklAgent;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionParameter;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.utils.StringUtil;

public abstract class BasicTool implements Tool {

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
    final Map<String,AsyncToolOut> users = new HashMap<>();

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

    public class ToolCallback implements Function<TriggerInput, Void>{
        @Override
        public Void apply(TriggerInput input) {
            if(users.getOrDefault(input.getUser(), null)!=null){
                memoryProvider.onAssistantResponsed(input.getUser());
                memoryProvider.onAssistantResponsed(input.getUser());
                users.get(input.getUser()).applyLater(input);
                users.remove(input.getUser());
            }
            return null;
        }
    } 

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
        return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, getObservationOnInvalidParameter(message))
                        .message(Tool.KEY_THOUGHT, getThoughtOnInvalidParameter())
                        .sync();
    }

    public AsyncToolOut waitUserInput(String user){
        AsyncToolOut out = ToolOuts.of(user, false)
                            .message(KEY_OBSERVATION, "")
                            .async();
        this.users.put(user, out);
        return out;
    }

    public ToolOut onResult(String user, String result){
        return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, result)
                        .message(Tool.KEY_THOUGHT, getThought())
                        .sync();
    }

    public ToolOut onToolError(String user){
        return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, getObservationOnError())
                        .message(Tool.KEY_THOUGHT, getThoughtOnError())
                        .sync();
    }

    public ToolOut onEmptyResult(String user){
        return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, getObservationOnEmptyResult())
                        .message(Tool.KEY_THOUGHT, getThoughtOnEmptyResult())
                        .sync();
    }

    public ToolOut endConversation(String user, String message){
        return ToolOuts.of(user, false)
                        .message(Tool.KEY_OBSERVATION, message)
                        .sync();
    }

    @Override
    public void onClearedMemory(String user) {
        this.users.remove(user);
    }

    @Override
    final public im.langchainjava.llm.entity.function.Function getFunction(){
        return im.langchainjava.llm.entity.function.Function.builder()
                .name(getName())
                .description(getFunctionDescription())
                .parameters(getFunctionParameters())
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
        List<String> requiredProperties = getFunctionRequiredProperties();
        if(requiredProperties != null && !requiredProperties.isEmpty() && call.getParsedArguments() != null){
            for(String r : requiredProperties){
                if(!call.getParsedArguments().containsKey(r)){
                    return invalidParameter(user, "Missing required parameter " + r + ".");
                }
                if(StringUtil.isNullOrEmpty(call.getParsedArguments().get(r))){
                    return invalidParameter(user, "Required parameter " + r + " is null or empty.");
                }
            }
        }

        return doInvoke(user, call);
    }

    public void registerTool(OneRoundMrklAgent agent){
        agent.registerTrigger(new ToolCallback());
    }

}
