package im.langchainjava.agent.controlledagent.model;

import static im.langchainjava.llm.LlmService.ROLE_ASSIS;
import static im.langchainjava.llm.LlmService.ROLE_FUNC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.controlledagent.Asserts;
import im.langchainjava.agent.controlledagent.EpisodicControlTool;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.utils.StringUtil;
import lombok.Data;
import lombok.NonNull;

@Data
public class Task{

    final Tool function;

    // final String tag;

    final boolean optional;

    final Map<String, String> extractions;

    final EpisodicControlTool episodicControlFunction;

    final List<ChatMessage> history;

    final Map<String, Task> inputs;

    final Map<String, String> result;

    AgentToolOut toolOut;

    boolean success;

    TaskFailure failure;

    public Task(Tool func, Map<String, String> param, @NonNull Map<String, String> extractions, boolean optional){

        this.function = func;
        this.optional = optional;
        this.extractions = extractions;
        this.success = false;
        this.history = new ArrayList<>();
        this.result = new HashMap<>();
        this.inputs = new HashMap<>();
        this.episodicControlFunction = new EpisodicControlTool(this);

        if(this.function == null || this.function.getFunctionProperties() == null){
            return;
        }

        Map<String, FunctionProperty> properties = this.function.getFunctionProperties();
        Map<String, ToolDependency> dependencies = this.function.getFunctionDependencies();

        for(Entry<String, FunctionProperty> e : properties.entrySet()){
            String propertyName = e.getKey();
            FunctionProperty p = e.getValue();

            if(p == null){
                continue;
            }

            Task subTask = null;
            boolean optnl = isOptional(this.function.getFunctionRequiredProperties(), propertyName);
            if(dependencies != null){
                ToolDependency td = dependencies.get(propertyName);
                if(td != null){
                    Tool depTool = td.getDependency();
                    Map<String, String> depExtractions = null;
                    if(td.getExtractions() == null || td.getExtractions().isEmpty()){
                        depExtractions = new HashMap<>();
                        depExtractions.put(propertyName, p.getDescription());
                    }else{
                        depExtractions = td.getExtractions();
                    }
                    subTask = new Task(depTool, null, depExtractions, optnl);
                }
            }

            if(subTask == null){
                Map<String, String> defaultExtractions = new HashMap<>();
                defaultExtractions.put(propertyName, p.getDescription());
                subTask = new Task(null, null, defaultExtractions, optnl);
            }
            
            Asserts.assertTrue(subTask != null, "Dependency is not defined for property: " + propertyName + " in function " + this.function.getName() + ".");

            if(param!= null){
                String value = param.get(propertyName);
                if(!StringUtil.isNullOrEmpty(value)){
                    subTask.finish(propertyName, value);
                }
            }

            inputs.put(propertyName, subTask);
        }

    }

    private static boolean isOptional(List<String> required, String propertyName){
        return (required == null || (!required.contains(propertyName))); 
    }

    public String getName() {
        return this.function.getName();
    }

    public boolean isFailed() {
        return this.failure != null;
    }

    public void finish(Map<String, String> results) {
        this.failure = null;
        this.success = true;
        for(Entry<String, String> e : results.entrySet()){
            this.result.put(e.getKey(), e.getValue());
        }
    }

    public void finish(String key, String value) {
        this.failure = null;
        this.success = true;
        this.result.put(key,value);
    }

    public void updateResult(Map<String, String> results){
        this.failure = null;
        this.success = false;
        for(Entry<String, String> e : results.entrySet()){
            this.result.put(e.getKey(), e.getValue());
        }
    }


    public void fail(TaskFailure failure) {
        this.failure = failure;
        this.success = false;
    }

    public void addAssistMessage(String user, ChatMessage message){
        this.history.add(message);
    }

    public void addAssistMessage(String user, String message){
        this.history.add(new ChatMessage(ROLE_ASSIS, message, user, null));
    }

    public void addFunctionCallResult(String user, String result){
        this.history.add(new ChatMessage(ROLE_FUNC, result, user, null));
    }

    public void addAssistFunctionCall(String user, FunctionCall call){
        this.history.add(new ChatMessage(ROLE_ASSIS, null, user, call));
    }

    public FunctionCall getFunctionCall(){
        Map<String, JsonNode> param = new HashMap<>();
        for(Entry<String, Task> dep : inputs.entrySet()){
            if(dep.getValue() == null || dep.getValue().getResult() == null){
                continue;
            }
            param.put(dep.getKey(),(JsonNode) dep.getValue().getResult());
        }
        return FunctionCall.builder()
                .name(this.getName())
                .parsedArguments(param)
                .build();
    }
}
