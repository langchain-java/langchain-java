package im.langchainjava.agent;

import java.util.List;
import java.util.function.Function;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.Tool.FunctionMessage;
public abstract class MemoryAgent extends FunctionCallAgent{

    public MemoryAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c, tools);
    }

    public abstract boolean onAssistantResponsed(String user, String content, int round); 

    public abstract void onCleardMemory(String user);

    @Override
    final public boolean onAssistantFunctionCall(String user, FunctionCall call, ToolOut output) {
        memoryProvider.onAssistantResponsed(user);
        memoryProvider.onReceiveFunctionCall(user, call);
        output.handlerForKey(BasicTool.KEY_FUNC_OUT, observation)
            .handlerForKey(BasicTool.KEY_THOUGHT, thought)
            .run();
        return onAssistantResponsed(user, null, memoryProvider.incrRoundAndGet(user));
    }

    @Override
    final public boolean onAssistantMessage(String user, String content){
        memoryProvider.onAssistantResponsed(user);
        memoryProvider.onReceiveAssisMessage(user, content);
        return onAssistantResponsed(user, content, memoryProvider.incrRoundAndGet(user)); 
    }

    @Override
    final public boolean onFunctionCallException(String user, Tool t, Exception e){
        memoryProvider.onReceiveFunctionCallResult(user, "Function Call Result: \n Exception: " + e.getMessage());
        memoryProvider.onReceiveAssisMessage(user, "Thought \n: Exception occurs during function call. I should try another function or inform the user with message `我不知道`.");
        return onAssistantResponsed(user, null, memoryProvider.incrRoundAndGet(user));
    }

    public void endConversation(String user){
        super.getMemoryProvider().reset(user);
        onCleardMemory(user);
    }

    public void rememberAssistantMessage(String user, String message){
        memoryProvider.onReceiveAssisMessage(user, message);
    }

    // observation
    final private Function<FunctionMessage, Void> observation = input -> {
        super.getMemoryProvider().onReceiveFunctionCallResult(input.getUser(), "Function Call Result: \n" + input.getMessage() + " \n");
        return null; 
    };

    // think
    final private Function<FunctionMessage, Void> thought = input -> {
        super.getMemoryProvider().onReceiveAssisMessage(input.getUser(), "Thought: " + input.getMessage() + " \n");
        return null;
    };

}
