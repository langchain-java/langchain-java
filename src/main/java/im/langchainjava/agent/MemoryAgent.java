package im.langchainjava.agent;

import java.util.List;
import java.util.function.Function;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.BasicChatMemory;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ControllorToolOut;
import im.langchainjava.tool.ControllorToolOut.Action;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOut.FunctionMessage;
public abstract class MemoryAgent extends FunctionCallAgent{

    private static int MAX_ROUNDS = 20;
    private static int MAX_FUNCS = 5;

    public MemoryAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c, tools);
    }

    public abstract boolean onAssistantInvoke(String user);

    public abstract void onAssistantResponsed(String user, String content, boolean isAssistantMessage); 
    public abstract void onMaxRound(String user);
    public abstract void onMaxFunctionCall(String user);
    public abstract void onAgentEndConversation(String user);
    public abstract void onCleardMemory(String user);

    @Override
    public boolean onInvokingAi(String user){
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
        }

        if(memoryProvider.getFunctionCallNum(user) >= MAX_FUNCS){
            onMaxFunctionCall(user);
        }

        return onAssistantInvoke(user);
    }

    @Override
    final public boolean onAssistantFunctionCall(String user, FunctionCall call, ToolOut output) {
        memoryProvider.onReceiveFunctionCall(user, call);
        output.handlerForKey(BasicTool.KEY_FUNC_OUT, observation)
        .handlerForKey(BasicTool.KEY_THOUGHT, thought)
        .run();
        memoryProvider.onAssistantResponsed(user);
        if(output instanceof ControllorToolOut){
            ControllorToolOut out = (ControllorToolOut)output;
            if(out.getAction() == Action.endConversation || out.getAction() == Action.finalAnswer){
                onAgentEndConversation(user);
            }
        }
        memoryProvider.incrFunctionCallAndGet(user);
        onAssistantResponsed(user, null, false);
        return true;
    }

    @Override
    final public boolean onAssistantMessage(String user, String content){
        memoryProvider.onReceiveAssisMessage(user, content);
        memoryProvider.onAssistantResponsed(user);
        onAssistantResponsed(user, content, true); 
        return true;
    }

    @Override
    final public boolean onFunctionCallException(String user, Tool t, Exception e){
        memoryProvider.onReceiveFunctionCallResult(user, "Function Call Result: \n Exception: " + e.getMessage());
        memoryProvider.onReceiveAssisMessage(user, "Thought \n: Exception occurs during function call. I should try another function or inform the user with message `我不知道`.");
        memoryProvider.incrFunctionCallAndGet(user);
        memoryProvider.onAssistantResponsed(user);
        onAssistantResponsed(user, null, false);
        return true;
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
