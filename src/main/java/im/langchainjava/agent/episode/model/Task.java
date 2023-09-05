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
import im.langchainjava.agent.episode.focus.FocusManager;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.AgentToolOut.AgentToolOutStatus;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.Data;

@Data
public class Task{

    final String user;

    final Task parent;

    final Tool function;

    Tool successor;

    final boolean optional;

    final boolean resolvable;

    final boolean generatable;

    final boolean directExtraction;

    final boolean forceGenerate;

    // final Map<String, String> extractions;
    TaskExtraction extraction;

    final EpisodicControlTool episodicControlFunction;

    final List<ChatMessage> history;

    final Map<String, Task> inputs;

    final Map<String, String> params;

    final List<String> dispatched;

    // final Map<String, String> result;
    String extracted;

    AgentToolOut toolOut;

    boolean success;

    TaskFailure failure;

    String name;

    public Task(FocusManager focusManager, String user, Task parent, Tool func, Map<String, String> param, TaskExtraction extraction, boolean optional, boolean resolvable, boolean generatable, boolean directExtraction, boolean forceGenerate){
        this.user = user;
        this.parent = parent;
        this.function = func;
        this.params = param;
        this.name = null;
        if(this.function != null){
            this.name = this.function.getName();
        }
        this.optional = optional;
        this.resolvable = resolvable;
        this.generatable = generatable;
        this.directExtraction = directExtraction;
        this.forceGenerate = forceGenerate;
        this.dispatched = new ArrayList<>();
        // this.extractions = extractions;
        this.extraction = extraction;
        this.success = false;
        this.history = new ArrayList<>();
        this.extracted = null;
        this.inputs = new HashMap<>();
        this.episodicControlFunction = new EpisodicControlTool(this, focusManager);

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
                    te = new TaskExtraction(propertyName, p.getDescription());
                    subTask = new Task(focusManager, this.user, this, depTool, null, te, optnl, td.isResolvable(), td.isGeneratable(), td.isDirectExtraction(), depTool.isForceGenerate()); 
                }
            }

            if(subTask == null){
                TaskExtraction te = new TaskExtraction(propertyName, p.getDescription());
                subTask = new Task(focusManager, this.user, this, null, null, te, optnl, false, true, false, false);
            }
            
            if(this.params!= null){
                String value = this.params.get(propertyName);
                if(!StringUtil.isNullOrEmpty(value)){
                    subTask.updateResult(value);
                    subTask.finish();
                }
            }

            inputs.put(propertyName, subTask);
        }

    }

    public void successor(Tool next){
        this.successor = next;
    } 

    public Task updateParams(FunctionCall call){
        Task outstanding = null;
        for(Entry<String, Task> e : this.inputs.entrySet()){
            String name = e.getKey();
            Task t = e.getValue();
            String val = ToolUtils.getStringParam(call, name);

            if(t == null){
                continue;
            }

            if(!t.isGeneratable()){
                continue;
            }

            if(StringUtil.isNullOrEmpty(val)){
                if(t.isSuccess()){
                    continue;
                }

                // t is not success                
                if(!t.isResolvable() && !t.isOptional()){
                    outstanding = t;
                }
                continue;
            }


            t.updateResult(val);
            t.finish();
        }
        return outstanding;
    }

    public Task getOutstandingUnresolvableDependency(){
        for(Entry<String, Task> e : this.inputs.entrySet()){
            Task t = e.getValue();
            if(t == null || t.isSuccess()){
                continue;
            }
            if(t.isOptional()){
                continue;
            }
            
            if(!t.isResolvable()){
                return t;
            }
        }
        return null;
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

    public List<ChatMessage> getStackHistory(){
        List<ChatMessage> stacked = new ArrayList<>();
        if(this.parent != null){
            stacked.addAll(parent.getStackHistory());
        }
        stacked.addAll(this.history);
        return stacked;
    }

    public void finish(){
        this.finish(null);
    }

    public void finish(Tool successor) {
        this.failure = null;
        this.success = true;

        if(successor != null){
            this.successor = successor;
        }
        
        if (this.parent == null){
            return;
        }
        
        if(this.toolOut != null && this.toolOut.getStatus() == AgentToolOutStatus.control){
            this.parent.addAssistMessage(this.user, this.toolOut.getOutput());
            return;
        }

        // unresolvable task and its function is not invoked.
        if(!this.isResolvable() && this.getToolOut() == null){
            return;
        }

        Asserts.assertTrue(!StringUtil.isNullOrEmpty(extracted), "Task has not extracted value when it is finished.");
        if (this.function == null){
            // this is non function call task
            this.parent.addAssistMessage(this.user, this.extraction.extraction + " is " + this.extracted);
            return;
        }

        if(this.toolOut == null){
            this.parent.addAssistMessage(this.user, this.extraction.extraction + " is " + this.extracted);
            return;
        }

        this.parent.addAssistFunctionCall(this.user, this.getFunctionCall());
        this.parent.addFunctionCallResult(this.user, this.getFunctionCall(), this.extracted);
    }


    public void updateResult(String result){
        this.failure = null;
        this.success = false;
        this.extracted = result;
    }

    public void fail(TaskFailure failure){
        this.fail(failure, null); 
    }

    public void fail(TaskFailure failure, Tool successor) {
        this.failure = failure;
        this.success = false;

        if(successor != null){
            this.successor = successor;
        }

        if (this.parent == null){
            return;
        }
        
        if (this.function == null){
            // this is non function call task
            this.parent.addAssistMessage(this.user, failure.getMessage());
            return;
        }

        Asserts.assertTrue(this.toolOut != null, "Function call task does not has a tool out upon failing.");

        if(this.toolOut.getStatus() == AgentToolOutStatus.control){
            this.parent.addAssistMessage(this.user, this.toolOut.getOutput());
            this.parent.addUserMessage(this.user, failure.getMessage());
            return;
        }


        this.parent.addAssistFunctionCall(this.user, this.getFunctionCall());
        this.parent.addFunctionCallResult(this.user, this.getFunctionCall(), failure.getMessage());
    }

    public void addUserMessage(String user, String message){
        this.history.add(new ChatMessage(ROLE_USER, message, user, null));
    }

    public void addAssistMessage(String user, String message){
        this.history.add(new ChatMessage(ROLE_ASSIS, message, null, null));
    }

    public void addFunctionCallResult(String user, FunctionCall call, String result){
        this.history.add(new ChatMessage(ROLE_FUNC, result, call.getName(), null));
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

    public boolean isReady(){
        for(Task t : inputs.values()){
            if(t == null){
                continue;
            }
            if(!t.isSuccess() && !t.isOptional()){
                return false;
            }
        }
        return true;
    }

    public String getPath(){
        if(this.parent == null){
            return "/" + this.getName();
        }
        return this.parent.getPath() + "/" + this.getName();
    }

    public void dispatched(String name){
        this.dispatched.add(name);
    }
}
