package im.langchainjava.agent.controlledagent.model;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Episode{

    final Stack<Task> stack;

    public Episode(@NonNull List<Task> tasks){
        stack = new Stack<>();
        addTasks(tasks);
    }

    public Episode(@NonNull Task task){
        stack = new Stack<>();
        addTask(task);
    }
    
    public Task addTasks(@NonNull List<Task> tasks){
        int size = tasks.size();
        for(int i = size - 1; i>=0; i--){
            if(tasks.get(i)!= null){
                stack.push(tasks.get(i));
            }
        }
        return getCurrentTask();
    }

    public Task addTask(@NonNull Task task){
        stack.push(task);
        return getCurrentTask();
    }

    public Task popCurrentTaskAndGetNext(){
        try{
            stack.pop();
            return getCurrentTask();
        }catch(EmptyStackException e){
            return null;
        }
    }

    public Task getCurrentTask(){
        try{
            return stack.peek();
        }catch(EmptyStackException e){
            return null;
        }
    }

}
