package im.langchainjava.agent.functioncall;

import im.langchainjava.agent.CommandAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.controlledagent.EpisodeException;
import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.agent.exception.AiResponseException;
import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.AgentToolOut;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FunctionCallAgent extends CommandAgent{

    final private TaskSolver solver;

    public FunctionCallAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, TaskSolver solver){
        super(llm, prompt, memory, c);
        this.solver = solver;
    }

    public abstract boolean onMessage(String user, String message, boolean isUserTurn);

    public abstract boolean onFunctionCallResult(String user, Tool tool, FunctionCall functionCall, AgentToolOut functionOut, boolean isUserTurn);
    
    public abstract boolean onFunctionCallException(String user, FunctionCall functionCall, Exception e, boolean isUserTurn);

    public abstract boolean onFunctionExecutionException(String user, Tool t, FunctionCall functionCall, Exception e, boolean isUserTurn);

    @Override
    public boolean onAiResponse(String user, ChatMessage message, boolean isUserTurn){
        if(message.getFunctionCall() != null){
            FunctionCall call = message.getFunctionCall();

            Task task = null;
            try{
                task = solver.solveFunctionCall(user, call);
            }catch(EpisodeException e){
                return onFunctionCallException(user, call, e, isUserTurn);
            }

            if(task == null){
                return onFunctionCallException(user, call, new EpisodeException("The resolved task is null on new function call task."), isUserTurn);
            }
                
            Tool func = task.getFunction();

            if(func == null){
                return onFunctionCallException(user, call, new EpisodeException("The resolved function call task has no function in it."), isUserTurn);
            }

            return handleFunctionCall(user, func, call, isUserTurn);
        }

        if(StringUtil.isNullOrEmpty(message.getContent())){
            onAiException(user, new AiResponseException("The assistant responsed an empty message."));
            return false;
        }

        return handleMessage(user, message.getContent(), isUserTurn);
    }


    private boolean handleFunctionCall(String user, Tool tool, FunctionCall call, boolean isUserTurn){
        try{
            onFunctionCall(user, call, isUserTurn);
            ToolOut toolOut = tool.invoke(user, call, getMemoryProvider());
            if(toolOut == null){
                return onFunctionExecutionException(user, tool, call, new FunctionCallException("Function call "+ tool.getFunction().getName() + " returns null."), isUserTurn);
            }
            if(!(toolOut instanceof AgentToolOut)){
                return onFunctionExecutionException(user, tool, call, new FunctionCallException("Function call "+ tool.getFunction().getName() + " does not return a agent tool out object."), isUserTurn);
            }
            return onFunctionCallResult(user, tool, call, (AgentToolOut) toolOut, isUserTurn);
        }catch(Exception e){
            return onFunctionExecutionException(user, tool, call, e, isUserTurn);
        }
        
    }

    private boolean handleMessage(String user, String message, boolean isUserTurn){
        return onMessage(user, message, isUserTurn);
    }


    public void onFunctionCall(String user, FunctionCall functionCall, boolean isUserTurn){
        // do nothing. Can be overrided.
        return;
    }

}
