package im.langchainjava.agent;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
public abstract class MemoryAgent extends FunctionCallAgent{

    private static int MAX_ROUNDS = 20;

    public MemoryAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c) {
        super(llm, prompt, memory, c);
    }

    public abstract boolean onAssistantFunctionCall(String user, FunctionCall functionCall, String content);

    public abstract boolean onAssistantMessage(String user, String content);

    public abstract void onMaxRound(String user);

    @Override
    public boolean onFunctionCall(String user, FunctionCall response, String content) {
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
            return true;
        }
        memoryProvider.onAssistantResponsed(user);
        memoryProvider.onReceiveFunctionCall(user, response);
        boolean assisResp = onAssistantFunctionCall(user, response, content); 
        return assisResp;
    }

    public boolean onMessage(String user, String content){
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
            return true;
        }
        memoryProvider.onAssistantResponsed(user);
        memoryProvider.onReceiveAssisMessage(user, content);
        boolean assisResp = onAssistantMessage(user, content); 
        return assisResp;
    }

    @Override
    public boolean onFunctionCallException(String user, Tool t, Exception e){
        memoryProvider.onReceiveFunctionCallResult(user, "Function Call Result: \n Exception: " + e.getMessage());
        memoryProvider.onReceiveAssisMessage(user, "Thought \n: Exception occurs during function call. I should try another function or inform the user with message `我不知道`.");
        return false;
    }

}
