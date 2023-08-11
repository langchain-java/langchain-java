package im.langchainjava.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionParameter;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.askuser.AskUserTool;
import im.langchainjava.tool.askuser.form.FormBuilder;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Tool {

    // public static String KEY_FUNC_OUT = "Function";
    // public static String KEY_THOUGHT = "Thought";
    // public static String KEY_CONTROL_SUMMARY = "Summary";
    // public static String KEY_CONTROL_ASK = "Ask";

    public static String PARAMETER_TYPE_OBJECT = "object";

    public static String OBSERVATION_ON_EMPTY = "This function does not give any result this time.";
    public static String OBSERVATION_ON_ERR = "This function is not available. Don't use this function again.";
    public static String EXTRACTION_NAME = "function_output";
    public static String EXTRACTION_ON_EMPTY = "There is no result. I should try another function or tell the user `我不知道`.";
    public static String EXTRACTION_ON_ERR = "I should try another function or tell the user `我不知道`.";
    public static String EXTRACTION = "Now I have the results. I should extract useful information from these results and think what action to take.";
    public static String OBSERVATION_ON_INVALID_PARAM = "Invalid parameters: ";
    public static String EXTRACTION_ON_INVALID_PARAM = "I should ask the user to clarify the question or try the function again with corrected parameter.";

    String observationOnEmptyResult;
    String observationOnError;
    String observationOnInvalidParameter;
    String extractionName;
    String extraction;
    String extractionOnEmptyResult;
    String extractionOnError;
    String extractionOnInvalidInputFormat;
    String desc;
    Map<String, FunctionProperty> parameters;
    List<String> required;
    // String tag;
    Map<String, ToolDependency> dependencies;

    // final public ChatMemoryProvider memoryProvider;
    // final Map<String,AsyncToolOut> users = new HashMap<>();

    public abstract String getName();
    public abstract String getDescription();
    public abstract Map<String, FunctionProperty> getProperties();
    public abstract List<String> getRequiredProperties();
    // public abstract FunctionProperty getOutput();
    // public abstract String getActionPrompt();
    // public abstract String getTag();
    public abstract Map<String, ToolDependency> getDependencies();
    public abstract ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory);

    public Tool(){
        // this.memoryProvider = memoryProvider;
        this.observationOnEmptyResult = null;
        this.observationOnError = null;
        this.observationOnInvalidParameter = null;
        this.extraction = null;
        this.extractionOnEmptyResult = null;
        this.extractionOnError = null;
        this.extractionOnInvalidInputFormat = null;
        this.extractionName = null;
        this.desc = null;
        this.parameters = null;
        this.required = null;
        this.dependencies = null;
        // this.tag = null;
    }

    public Tool description(String desc){
        this.desc = desc;
        return this;
    }

    public Tool inputFormat(Map<String, FunctionProperty> param){
        this.parameters = param;
        return this;
    }

    public Tool required(List<String> required){
        this.required =required;
        return this;
    }

    public Tool observationOnInvalidInputFormat(String obs){
        this.observationOnInvalidParameter = obs;
        return this;
    }

    public Tool observationOnEmptyResult(String obs){
        this.observationOnEmptyResult = obs;
        return this;
    }

    public Tool observationOnError(String obs){
        this.observationOnError = obs;
        return this;
    }

    public Tool extractionName(String name){
        this.extractionName = name;
        return this;
    }

    public Tool extraction(String th){
        this.extraction = th;
        return this;
    }

    public Tool extractionOnEmptyResult(String th){
        this.extractionOnEmptyResult = th;
        return this;
    }


    public Tool extractionOnError(String th){
        this.extractionOnError = th;
        return this;
    }

    public Tool extractionOnInvalidInputFormat(String th){
        this.extractionOnInvalidInputFormat = th;
        return this;
    }

    public Tool dependencyAndProperty(ImService im, FormBuilder formBuilder){
        return dependencyAndProperty(im, formBuilder, true);
    }

    public Tool dependencyAndProperty(String name, String description, Tool tool, boolean isRequired){
        if(this.dependencies == null){
            this.dependencies = new HashMap<>();
        }
        if(this.parameters == null){
            this.parameters = new HashMap<>();
        }
        if(this.required == null){
            this.required = new ArrayList<>();
        }

        FunctionProperty p = FunctionProperty.builder()
                .description(description)
                .build();
        this.parameters.put(name, p);

        ToolDependency td = ToolDependency.builder()
                .dependency(tool)
                .build();
        this.dependencies.put(name, td);

        if(isRequired){
            this.required.add(name);
        }

        return this;
    }

    public Tool dependencyAndProperty(ImService im, FormBuilder formBuilder, boolean isRequired){
        String property = formBuilder.getName();
        String description = formBuilder.getDescription();

        if(this.dependencies == null){
            this.dependencies = new HashMap<>();
        }
        if(this.parameters == null){
            this.parameters = new HashMap<>();
        }
        if(this.required == null){
            this.required = new ArrayList<>();
        }

        FunctionProperty p = FunctionProperty.builder()
                .description(description)
                .build();
        this.parameters.put(property, p);

        ToolDependency td = ToolDependency.builder()
                .dependency(new AskUserTool(im, formBuilder))
                .build();
        this.dependencies.put(property, td);

        if(isRequired){
            this.required.add(property);
        }

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

    public String getExtractionName(){
        return (this.extractionName != null) ? this.extractionName : EXTRACTION_NAME;
    }

    public String getExtraction(){
        return (this.extraction != null) ? this.extraction : EXTRACTION;
    }

    public String getExtractionOnEmptyResult(){
        return (this.extractionOnEmptyResult != null) ? this.extractionOnEmptyResult : EXTRACTION_ON_EMPTY;
    }

    public String getExtractionOnError(){
        return (this.extractionOnError != null) ? this.extractionOnError : EXTRACTION_ON_ERR;
    }

    public String getExtractionOnInvalidParameter(){
        return (this.extractionOnInvalidInputFormat != null) ? this.extractionOnInvalidInputFormat : EXTRACTION_ON_INVALID_PARAM;
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
    
    final public Map<String, ToolDependency> getFunctionDependencies(){
        if(this.dependencies != null){
            return this.dependencies;
        }
        return getDependencies();
    }

    // final public String getFunctionTag(){
    //     if(this.tag != null){
    //         return this.tag;
    //     }
    //     return getTag();
    // }

    final public FunctionParameter getFunctionParameters() {
        return FunctionParameter.builder()
                .type(PARAMETER_TYPE_OBJECT)
                .properties(getFunctionProperties())
                .required(getFunctionRequiredProperties())
                .build();
    }

    // public String getWrappedAssisMessage(FunctionCall call, AgentToolOut out){
    //     return null;
    // }

    // public ChatMessage getAssistantMessage(String user, FunctionCall call, AgentToolOut out){
    //     String wrapped = getWrappedAssisMessage(call, out);
    //     if(wrapped != null){
    //         return new ChatMessage(LlmService.ROLE_ASSIS, wrapped, user, null);
    //     }
    //     return new ChatMessage(LlmService.ROLE_ASSIS, null, user, call);
    // }

    // public ChatMessage getFunctionOutputMessage(String user, AgentToolOut toolOut){
    //     if(toolOut.getStatus() == AgentToolOutStatus.success && !StringUtil.isNullOrEmpty(toolOut.getOutput())){
    //         return new ChatMessage(LlmService.ROLE_FUNC, toolOut.getOutput(), user, null);
    //     }
    //     if(toolOut.getStatus() != AgentToolOutStatus.success && !StringUtil.isNullOrEmpty(toolOut.getError())){
    //         return new ChatMessage(LlmService.ROLE_FUNC, toolOut.getError(), user, null);
    //     }
    //     return null;
    // }

    // @Override
    // public void onClearedMemory(String user) {
    //     // this.users.remove(user);
    // }

    final public im.langchainjava.llm.entity.function.Function getFunction(){
        return im.langchainjava.llm.entity.function.Function.builder()
                .name(getName())
                .description(getFunctionDescription())
                .parameters(getFunctionParameters())
                .build();
    }
    
    final public im.langchainjava.llm.entity.function.FunctionCall getFunctionCall(){
        return im.langchainjava.llm.entity.function.FunctionCall.builder()
                .name(getName())
                .build();
    }

    final public ToolOut invoke(String user, FunctionCall call, ChatMemoryProvider memory){
        if(call == null ){
            throw new FunctionCallException("The function call is null!");
        }
        if(!call.getName().equals(getName())){
            throw new FunctionCallException("The function name does not match function call. Function name is "+ getName() + " while the function call is " + call.getName() + "." );
        }
        Map<String, JsonNode> params = parseFunctionCallParam(call);
        if(params == null){
            return ToolOuts.invalidParameter(user, "Could not parse parameters for function " + call.getName() + ".");
        }
        call.setParsedArguments(params);

        List<String> requiredProperties = getFunctionRequiredProperties();
        if(requiredProperties != null && !requiredProperties.isEmpty() && call.getParsedArguments() != null){
            for(String r : requiredProperties){
                if(!call.getParsedArguments().containsKey(r)){
                    log.info("Missing required parameter " + r + ".");
                    // return invalidParameter(user, "Missing required parameter " + r + ".");
                }
            }
        }

        return doInvoke(user, call, memory);
    }

    public static Map<String, JsonNode> parseFunctionCallParam(FunctionCall call){

        String rawArguments = call.getArguments();
        Map<String, JsonNode> params = null;
        if(!StringUtil.isNullOrEmpty(rawArguments)){
            params = JsonUtils.toMapOfJsonNode(rawArguments);
        }

        return params;
    }

}
