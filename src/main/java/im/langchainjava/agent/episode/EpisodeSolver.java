package im.langchainjava.agent.episode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.episode.model.Episode;
import im.langchainjava.agent.episode.model.Task;
import im.langchainjava.agent.episode.model.TaskBuilder;
import im.langchainjava.agent.episode.model.TaskExtraction;
import im.langchainjava.agent.functioncall.TaskSolver;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;
import lombok.NonNull;

public class EpisodeSolver implements TaskSolver{

    public static String CONTEXT_KEY_TASK = "episode";

    final ChatMemoryProvider memory;
    final Map<String, Tool> tools;
    
    TaskBuilder initialTaskBuilder;
    
    public EpisodeSolver(ChatMemoryProvider memory, List<Tool> tools){
        Asserts.assertTrue(tools != null && !tools.isEmpty(), "Tools can not be null or empty.");
        
        this.memory = memory;
        this.tools = new HashMap<>();
        for(Tool t : tools){
            Asserts.assertTrue(t != null && !StringUtil.isNullOrEmpty(t.getName()), "One of the given tools are null or without a tool name.");
            this.tools.put(t.getName(), t);
        }
    }

    public void initialTask(TaskBuilder initialTaskBuilder){
        this.initialTaskBuilder = initialTaskBuilder; 
    }

    public Task getCurrentTask(String user){
        Episode e = getEpisode(user);
        Asserts.assertTrue(e != null, "The eposide is null for user "+ user + ".");
        Task task = e.getCurrentTask();
        while(task != null && taskIsPassed(task)){
            task = e.popCurrentTaskAndGetNext();
        }
        return task;
    }

    public Task popCurrentTask(String user){
        Episode e = getEpisode(user);
        return e.popCurrentTaskAndGetNext();
    }

    public Task resolveCurrentTask(@NonNull String user){

        Episode e = getEpisode(user);
        Asserts.assertTrue(e != null, "The eposide is null for user "+ user + ".");

        //pick up from what's left
        Task task = getCurrentTask(user);

        if(task == null){
            return null;
        }
        if(task.isFailed()){
            return task;
        }

        // solve dependencies
        Task solvedTask = null;
        if(task.getInputs() != null){
            for(Task t : task.getInputs().values()){
                // the unresolvable dependencies does not need to solve again.
                if(taskIsUnresolvable(t)){
                    continue;
                }

                e.addTask(t);
                solvedTask = resolveCurrentTask(user);
                
                //solvedTask is passed
                if(solvedTask == null){
                    continue;
                }

                if(solvedTask.isFailed()){
                    return solvedTask;
                };

                task = solvedTask;
            }
        }

        return task;
    }

    public Episode getEpisode(String user){
        Episode episode = (Episode) memory.getContextForUser(user, CONTEXT_KEY_TASK, null);
        if(episode == null){
            // init a root task if there is no episode. (This happens only at the start of the episode)
            List<Task> initialTasks = this.initialTaskBuilder.build(user);
            episode = new Episode(initialTasks);
            memory.setContextForUser(user, CONTEXT_KEY_TASK, episode);
        }
        return episode;
    }

    private boolean taskIsPassed(Task task){
        return (task == null || task.isSuccess() || (task.isOptional() && task.isFailed()));
    }

    private boolean taskIsUnresolvable(Task task){
        return (task == null 
                || task.isSuccess() 
                || task.isFailed()
                || task.isOptional() 
                || !task.isResolvable());
    }

    @Override
    public Task solveFunctionCall(String user, FunctionCall call, Tool given) {
        
        Asserts.assertTrue(call != null && call.getName() != null, "Function call is null or without a name.");
        
        Tool t = given;
        if(t == null){
            Asserts.assertTrue(this.tools.containsKey(call.getName()), "There is no tool matching function call " + call.getName() + ".");
            t = tools.get(call.getName());
        }

        TaskExtraction te = new TaskExtraction(t.getExtractionName(), t.getExtraction());
        Map<String, JsonNode> parsedParam = Tool.parseFunctionCallParam(call);
        call.setParsedArguments(parsedParam);

        Map<String, String> param = new HashMap<>();
        for(Entry<String, JsonNode> e : parsedParam.entrySet()){
            param.put(e.getKey(), e.getValue().asText());
        }

        // Task parent = this.solveCurrentTask(user);
        Task parent = getCurrentTask(user);

        Asserts.assertTrue(parent != null && !parent.isFailed(), "Parent task is null or is failed prematurely.");

        Task task = new Task(null, user, parent, t, param, te, true, true, true, false, t.isForceGenerate());
        // parent.input(te.getName(), task);
        
        getEpisode(user).addTask(task);
        return this.resolveCurrentTask(user);
    }
    
}
