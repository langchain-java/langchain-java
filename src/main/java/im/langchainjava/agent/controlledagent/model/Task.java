package im.langchainjava.agent.controlledagent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import im.langchainjava.agent.controlledagent.Asserts;
import im.langchainjava.agent.controlledagent.EpisodicControlTool;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import lombok.Data;
import lombok.NonNull;

@Data
public class Task{

    final Tool function;

    final String tag;

    final boolean optional;

    final Map<String, String> extractions;

    final EpisodicControlTool episodicControlFunction;

    final List<ChatMessage> history;

    final Map<String, Task> inputs;

    Object result;

    boolean success;

    TaskFailure failure;

    public Task(Tool func, Map<String, Object> param, @NonNull String tag, @NonNull Map<String, String> extractions, boolean optional){

        this.function = func;
        this.tag = tag;
        this.optional = optional;
        this.extractions = extractions;
        this.result = null;
        this.success = false;
        this.inputs = new HashMap<>();
        this.history = new ArrayList<>();
        this.episodicControlFunction = new EpisodicControlTool(this);

        if(this.function == null || this.function.getFunctionProperties() == null){
            return;
        }

        Map<String, FunctionProperty> params = this.function.getFunctionProperties();
        Map<String, ToolDependency> dependencies = this.function.getDependencies();

        for(Entry<String, FunctionProperty> e : params.entrySet()){
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
                    Map<String, String> depExtractions = td.getExtractions();
                    subTask = new Task(depTool, null, p.getTag(), depExtractions, optnl);
                }        
            }
            
            Asserts.assertTrue(subTask != null, "Dependency is not defined for property: " + propertyName + " in function " + this.function.getName() + ".");

            if(param!= null){
                Object value = param.get(propertyName);
                if(value != null){
                    subTask.finish(value);
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

    public void finish(Object result) {
        this.failure = null;
        this.result = result;
        this.success = true;
    }

    public void fail(TaskFailure failure) {
        this.failure = failure;
        this.success = false;
    }

    public void addAssistMessage(String message){
        this.history.add(new ChatMessage(ROLE_ASSISTANT, message));
    }

    public FunctionCall getFunctionCall(){
        FunctionCall call = FunctionCall.builder()

                .arguments()
    }
}
