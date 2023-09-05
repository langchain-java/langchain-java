package im.langchainjava.agent.functioncall;

import im.langchainjava.agent.CommandAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.exception.AiResponseException;
import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FunctionCallAgent extends CommandAgent{

    public FunctionCallAgent(ChatMemoryProvider memory, CommandParser c){
        super(memory, c);
    }

    public abstract boolean onAgentMessage(String user, String message, boolean isUserTurn);

    public abstract boolean onFunctionCall(String user, FunctionCall call, boolean isUserTurn, FunctionCall givenCall);

    public abstract boolean onFunctionCallResult(String user, Tool tool, FunctionCall functionCall, AgentToolOut functionOut, boolean isUserTurn);
    
    public abstract boolean onFunctionCallException(String user, FunctionCall functionCall, Exception e, boolean isUserTurn);

    public abstract boolean onFunctionExecutionException(String user, Tool t, FunctionCall functionCall, Exception e, boolean isUserTurn);

    public boolean onAiResponse(String user, ChatMessage message, boolean isUserTurn, FunctionCall givenCall){
        if(message.getFunctionCall() != null){
            FunctionCall call = message.getFunctionCall();
            return onFunctionCall(user, call, isUserTurn, givenCall);
        }

        if(StringUtil.isNullOrEmpty(message.getContent())){
            onAiException(user, new AiResponseException("The assistant responsed an empty message."));
            return false;
        }

        return handleMessage(user, message.getContent(), isUserTurn);
    }

    public boolean handleFunctionCall(String user, Tool tool, FunctionCall call, boolean isUserTurn){
        try{
            ToolOut toolOut = tool.invoke(user, call, getMemoryProvider());
            if(toolOut == null){
                return onFunctionExecutionException(user, tool, call, new FunctionCallException("Function call "+ tool.getFunction().getName() + " returns null."), isUserTurn);
            }
            if(!(toolOut instanceof AgentToolOut)){
                return onFunctionExecutionException(user, tool, call, new FunctionCallException("Function call "+ tool.getFunction().getName() + " does not return a agent tool out object."), isUserTurn);
            }
            return onFunctionCallResult(user, tool, call, (AgentToolOut) toolOut, isUserTurn);
        }catch(Exception e){
            e.printStackTrace();
            return onFunctionExecutionException(user, tool, call, e, isUserTurn);
        }
    }

    private boolean handleMessage(String user, String message, boolean isUserTurn){
        return onAgentMessage(user, message, isUserTurn);
    }

}
