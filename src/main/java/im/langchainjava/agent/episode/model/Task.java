package im.langchainjava.agent.episode.model;

import static im.langchainjava.llm.LlmService.ROLE_ASSIS;
import static im.langchainjava.llm.LlmService.ROLE_FUNC;
import static im.langchainjava.llm.LlmService.ROLE_USER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import im.langchainjava.agent.episode.Asserts;
import im.langchainjava.agent.episode.EpisodicControlTool;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.Data;
import lombok.NonNull;

@Data
public class Task{

    final Tool function;

    // final String tag;

    final boolean optional;

    // final Map<String, String> extractions;
    TaskExtraction extraction;

    final EpisodicControlTool episodicControlFunction;

    final List<ChatMessage> history;

    final Map<String, Task> inputs;

    // final Map<String, String> result;
    String extracted;

    AgentToolOut toolOut;

    boolean success;

    TaskFailure failure;

    String name;

    // public Task(Tool func, Map<String, String> param, @NonNull Map<String, String> extractions, boolean optional){
    public Task(Tool func, Map<String, String> param, TaskExtraction extraction, boolean optional){

        this.function = func;
        this.name = null;
        if(this.function != null){
            this.name = this.function.getName();
        }
        this.optional = optional;
        // this.extractions = extractions;
        this.extraction = extraction;
        this.success = false;
        this.history = new ArrayList<>();
        this.extracted = null;
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
                    TaskExtraction te = null;
                    // Map<String, String> depExtractions = null;
                    // if(td.getExtractions() == null || td.getExtractions().isEmpty()){
                    if(StringUtil.isNullOrEmpty(td.getExtraction())){
                        te = new TaskExtraction(propertyName, p.getDescription());
                        // depExtractions = new HashMap<>();
                        // depExtractions.put(propertyName, p.getDescription());
                    }else{
                        te = new TaskExtraction(propertyName, td.getExtraction());
                        // depExtractions = td.getExtractions();
                    }
                    subTask = new Task(depTool, null, te, optnl);
                }
            }

            if(subTask == null){
                // Map<String, String> defaultExtractions = new HashMap<>();
                // defaultExtractions.put(propertyName, p.getDescription());
                TaskExtraction te = new TaskExtraction(propertyName, p.getDescription());
                subTask = new Task(null, null, te, optnl);
            }
            
            Asserts.assertTrue(subTask != null, "Dependency is not defined for property: " + propertyName + " in function " + this.function.getName() + ".");

            if(param!= null){
                String value = param.get(propertyName);
                if(!StringUtil.isNullOrEmpty(value)){
                    subTask.finish(value);
                }
            }

            inputs.put(propertyName, subTask);
        }

    }

    public void input(String key, Task task){
        this.inputs.put(key, task);
    }

    private static boolean isOptional(List<String> required, String propertyName){
        return (required == null || (!required.contains(propertyName))); 
    }

    public String getName() {
        return this.name;
    }

    public boolean isFailed() {
        return this.failure != null;
    }

    public void finish(String result) {
        this.failure = null;
        this.success = true;
        // for(Entry<String, String> e : results.entrySet()){
        //     this.result.put(e.getKey(), e.getValue());
        // }
        this.extracted = result;
    }


    public void updateResult(String result){
        this.failure = null;
        this.success = false;
        this.extracted = result;
    }


    public void fail(TaskFailure failure) {
        this.failure = failure;
        this.success = false;
    }

    public void addUserMessage(String user, String message){
        this.history.add(new ChatMessage(ROLE_USER, message, user, null));
    }

    public void addAssistMessage(String user, String message){
        this.history.add(new ChatMessage(ROLE_ASSIS, message, null, null));
    }

    public void addFunctionCallResult(String user, String result){
        this.history.add(new ChatMessage(ROLE_FUNC, result, user, null));
    }

    public void addAssistFunctionCall(String user, FunctionCall call){
        this.history.add(new ChatMessage(ROLE_ASSIS, "none", null, call));
    }

    public FunctionCall getFunctionCall(){
        Map<String, JsonNode> param = new HashMap<>();
        Map<String, String> strParam = new HashMap<>();
        for(Entry<String, Task> dep : inputs.entrySet()){
            if(dep.getValue() == null || StringUtil.isNullOrEmpty(dep.getValue().getExtracted())){
                continue;
            }
            param.put(dep.getKey(),  TextNode.valueOf(dep.getValue().getExtracted()));
            strParam.put(dep.getKey(), dep.getValue().getExtracted());
        }
        return FunctionCall.builder()
                .name(this.getName())
                .parsedArguments(param)
                .arguments(JsonUtils.fromMap(strParam))
                .build();
    }
}
