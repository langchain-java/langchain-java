package im.langchainjava.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.agent.AsyncAgent.TriggerInput;
import im.langchainjava.agent.mrklagent.OneRoundMrklAgent;
import im.langchainjava.memory.ChatMemoryProvider;

public abstract class BasicTool implements Tool {

    public static String OBSERVATION_ON_EMPTY = "This tool does not give any result this time.";
    public static String OBSERVATION_ON_ERR = "This tool is not available. Don't use this tool again.";
    public static String THOUGHT_ON_EMPTY = "There is no result. I should try another tool or tell the user `我不知道`.";
    public static String THOUGHT_ON_ERR = "I should try another tool or tell the user `我不知道`.";
    public static String THOUGHT = "Now I have the results. I should extract useful information from these results and inform the user.";
    public static String OBSERVATION_ON_INVALID_INPUT = "The `Action Input` is in wrong format.";
    public static String THOUGT_ON_INVALID_INPUT = "I should try this tool again with the following input format:\n";

    String observationOnEmptyResult;
    String observationOnError;
    String observationOnInvalidInputFormat;
    String thought;
    String thoughtOnEmptyResult;
    String thoughtOnError;
    String thoughtOnInvalidInputFormat;
    String desc;
    String input;

    final public ChatMemoryProvider memoryProvider;
    final Map<String,AsyncToolOut> users = new HashMap<>();

    public abstract String getDescription();
    public abstract String getInputFormat();

    public BasicTool(ChatMemoryProvider memoryProvider){
        this.memoryProvider = memoryProvider;
        this.observationOnEmptyResult = null;
        this.observationOnError = null;
        this.observationOnInvalidInputFormat = null;
        this.thought = null;
        this.thoughtOnEmptyResult = null;
        this.thoughtOnError = null;
        this.thoughtOnInvalidInputFormat = null;
        this.desc = null;
        this.input = null;
    }

    public class ToolCallback implements Function<TriggerInput, Void>{
        @Override
        public Void apply(TriggerInput input) {
            if(users.getOrDefault(input.getUser(), null)!=null){
                memoryProvider.drainPendingMessages(input.getUser());
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

    public BasicTool inputFormat(String input){
        this.input = input;
        return this;
    }

    public BasicTool observationOnInvalidInputFormat(String obs){
        this.observationOnInvalidInputFormat = obs;
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

    public String getObservationOnInvalidInputFormat(){
        return (this.observationOnInvalidInputFormat != null) ? this.observationOnInvalidInputFormat : OBSERVATION_ON_INVALID_INPUT;
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

    public String getThoughtOnInvalidInputFormat(){
        String prefix = (this.thoughtOnInvalidInputFormat != null) ? this.thoughtOnInvalidInputFormat : THOUGT_ON_INVALID_INPUT;
        return prefix + " " + getToolInputFormat();
    }

    @Override
    final public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return getDescription();
    }
    
    @Override
    final public String getToolInputFormat() {
        if(this.input != null){
            return this.input;
        }
        return getInputFormat(); 
    }

    public ToolOut invalidInputFormat(String user){
        return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, getObservationOnInvalidInputFormat())
                        .message(Tool.KEY_THOUGHT, getThoughtOnInvalidInputFormat())
                        .sync();
    }

    public AsyncToolOut waitUserInput(String user){
        AsyncToolOut out = ToolOuts.of(user, false)
                                .message(Tool.KEY_OBSERVATION, "")
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

    public void registerTool(OneRoundMrklAgent agent){
        agent.registerTrigger(new ToolCallback());
    }

}
