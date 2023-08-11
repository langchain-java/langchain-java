package im.langchainjava.agent;

import java.util.List;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.controlledagent.EpisodicAgent;
import im.langchainjava.agent.controlledagent.EpisodicPromptProvider;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;
public abstract class MemoryAgent extends EpisodicAgent{

    private static int MAX_ROUNDS = 20;
    private static int MAX_FUNCS = 5;

    final private ChatMemoryProvider memoryProvider;

    public MemoryAgent(LlmService llm, EpisodicPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools) {
        super(llm, prompt, memory, c);
        this.memoryProvider = memory;
    }

    public abstract void onAssistantResponsed(String user, String content, boolean isAssistantMessage); 
    public abstract void onMaxRound(String user);
    public abstract void onMaxFunctionCall(String user);
    // public abstract void onAgentEndConversation(String user);
    public abstract void onCleardMemory(String user);

    @Override
    public boolean onInvokingAi(String user, boolean isUserTurn){
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
            endConversation(user);
            return false;
        }

        if(memoryProvider.getFunctionCallNum(user) >= MAX_FUNCS){
            onMaxFunctionCall(user);
            endConversation(user);
            return false;
        }

        return super.onInvokingAi(user, isUserTurn);
    }

    @Override
    final public void onAssistantFunctionCall(String user, Tool tool, FunctionCall call, AgentToolOut output, boolean isUserTurn) {
        memoryProvider.incrFunctionCallAndGet(user);
        memoryProvider.onReceiveFunctionCall(user, call);
        memoryProvider.onReceiveFunctionCallResult(user, output.getOutput());
        memoryProvider.onAssistantResponsed(user);
    }

    @Override
    final public void onAssistantMessage(String user, String content){
        memoryProvider.onReceiveAssisMessage(user, content);
        memoryProvider.onAssistantResponsed(user);
        onAssistantResponsed(user, content, true); 
    }

    @Override
    final public void onAssistantFunctionCallError(String user, FunctionCall functionCall, Exception e, boolean isUserTurn){
        memoryProvider.onReceiveFunctionCall(user, functionCall);
        if(e != null && !StringUtil.isNullOrEmpty(e.getMessage())){
            memoryProvider.onReceiveFunctionCallResult(user, "Function Call Failed: \n Exception: " + e.getMessage());
        }else{
            memoryProvider.onReceiveFunctionCallResult(user, "Function Call Failed: \n There is an unknown exception on function call.");
        }
        memoryProvider.incrFunctionCallAndGet(user);
        memoryProvider.onAssistantResponsed(user);
    }

    public void endConversation(String user){
        super.getMemoryProvider().reset(user);
        onCleardMemory(user);
    }

    // public void rememberAssistantMessage(String user, String message){
    //     memoryProvider.onReceiveAssisMessage(user, message);
    // }

}
