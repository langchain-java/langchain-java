package im.langchainjava.agent;

import java.util.List;
import java.util.function.Function;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.Tool.FunctionMessage;
public abstract class MemoryAgent extends FunctionCallAgent{

    private static int MAX_ROUNDS = 20;

    public MemoryAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c, tools);
    }

    // public abstract boolean onAssistantFunctionCall(String user, FunctionCall functionCall, ToolOut output);

    public abstract boolean onAssistantResponsed(String user, String content);

    public abstract void onMaxRound(String user);

    @Override
    public boolean onAssistantFunctionCall(String user, FunctionCall call, ToolOut output) {
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
            return true;
        }
        memoryProvider.onAssistantResponsed(user);
        memoryProvider.onReceiveFunctionCall(user, call);
        output.handlerForKey(Tool.KEY_OBSERVATION, observation)
            .handlerForKey(Tool.KEY_DISCLOSE, disclose)
            .handlerForKey(Tool.KEY_THOUGHT, thought)
            .apply(null);
        return onAssistantResponsed(user, null);
    }

    @Override
    public boolean onAssistantMessage(String user, String content){
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
            return true;
        }
        memoryProvider.onAssistantResponsed(user);
        memoryProvider.onReceiveAssisMessage(user, content);
        return onAssistantResponsed(user, content); 
    }

    @Override
    public boolean onFunctionCallException(String user, Tool t, Exception e){
        memoryProvider.onReceiveFunctionCallResult(user, "Function Call Result: \n Exception: " + e.getMessage());
        memoryProvider.onReceiveAssisMessage(user, "Thought \n: Exception occurs during function call. I should try another function or inform the user with message `我不知道`.");
        return false;
    }


    // observation
    final private Function<FunctionMessage, Void> observation = input -> {
        super.getMemoryProvider().onReceiveFunctionCallResult(input.getUser(), "Function Call Result: \n" + input.getMessage() + " \n");
        return null; 
    };

    // disclose
    final private Function<FunctionMessage, Void> disclose = input -> {
        super.getMemoryProvider().onReceiveAssisMessage(input.getUser(), input.getMessage() + " \n");
        return null;
    };

    // think
    final private Function<FunctionMessage, Void> thought = input -> {
        super.getMemoryProvider().onReceiveAssisMessage(input.getUser(), "Thought: " + input.getMessage() + " \n");
        return null;
    };

}
