package im.langchainjava.agent.controlledagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.controlledagent.model.Episode;
import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.agent.controlledagent.model.TaskBuilder;
import im.langchainjava.agent.functioncall.TaskSolver;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;
import lombok.NonNull;

public class EpisodeSolver implements TaskSolver{

    public static String CONTEXT_KEY_TASK = "episode";

    final TaskBuilder initialTaskBuilder;
    final ChatMemoryProvider memory;
    final Map<String, Tool> tools;

    public EpisodeSolver(ChatMemoryProvider memory, List<Tool> tools, TaskBuilder initialTaskBuilder){
        Asserts.assertTrue(tools != null && !tools.isEmpty(), "Tools can not be null or empty.");
        this.initialTaskBuilder = initialTaskBuilder; 
        this.memory = memory;
        this.tools = new HashMap<>();
        for(Tool t : tools){
            Asserts.assertTrue(t != null && !StringUtil.isNullOrEmpty(t.getName()), "One of the given tools are null or without a tool name.");
            this.tools.put(t.getName(), t);
        }
    }

    public Task solveCurrentTask(@NonNull String user){

        Episode e = getEpisode(user);
        Asserts.assertTrue(e != null, "The eposide is null for user "+ user + ".");

        //pick up from what's left
        Task task = e.getCurrentTask();
        //skip optional tasks
        while(task != null && taskIsPassed(task)){
            task = e.popCurrentTaskAndGetNext();
        }
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
                // the successful dependencies does not need to solve again.
                if(taskIsPassed(t)){
                    continue;
                }

                e.addTask(t);
                solvedTask = solveCurrentTask(user);
                
                //solvedTask is passed
                if(solvedTask == null){
                    continue;
                }

                if(solvedTask.isFailed()){
                    return solvedTask;
                };
            }
        }

        return task;
    }

    public Episode getEpisode(String user){
        Episode episode = (Episode) memory.getContextForUser(user, CONTEXT_KEY_TASK, null);
        if(episode == null){
            // init a root task if there is no episode. (This happens only at the start of the episode)
            List<Task> initialTasks = this.initialTaskBuilder.build();
            episode = new Episode(initialTasks);
            memory.setContextForUser(user, CONTEXT_KEY_TASK, episode);
        }
        return episode;
    }

    private boolean taskIsPassed(Task task){
        return (task == null || task.isSuccess() || task.isOptional());
    }

    @Override
    public Task solveFunctionCall(String user, FunctionCall call) {
        
        Asserts.assertTrue(call != null && call.getName() != null, "Function call is null or without a name.");
        Asserts.assertTrue(this.tools.containsKey(call.getName()), "There is no tool matching function call " + call.getName() + ".");

        Tool t = tools.get(call.getName());

        Map<String, String> extractions = new HashMap<>();
        extractions.put(t.getExtractionName(), t.getExtraction());
        Map<String, String> param = new HashMap<>();
        for(Entry<String, JsonNode> e : call.getParsedArguments().entrySet()){
            param.put(e.getKey(), e.getValue().asText());
        }

        Task task = new Task(t, param, extractions, false);
        
        getEpisode(user).addTask(task);
        return this.solveCurrentTask(user);
    }
    
}
