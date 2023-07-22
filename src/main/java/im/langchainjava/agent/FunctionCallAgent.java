package im.langchainjava.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolOut;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FunctionCallAgent extends CommandAgent{

    public FunctionCallAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c, List<Tool> tools){
        super(llm, prompt, memory, c);
        this.tools = new HashMap<>();
        for(Tool t: tools){
            this.tools.put(t.getFunction().getName(), t);
        }
    }

    public abstract boolean onAssistantFunctionCall(String user, FunctionCall functionCall, ToolOut functionOut);
    
    public abstract boolean onAssistantMessage(String user, String message);
    
    public abstract boolean onFunctionCallException(String user, Tool t, Exception e);

    Map<String, Tool> tools;

    @Override
    public boolean onAiResponse(String user, ChatMessage message){
        if(message.getFunctionCall() != null){
            FunctionCall call = message.getFunctionCall();
            return callFunction(user, call);
        }

        return onAssistantMessage(user, message.getContent());
    }


    public boolean callFunction(String user, FunctionCall functionCall){

        if(this.tools.containsKey(functionCall.getName())){
            Tool t = this.tools.get(functionCall.getName());
            try{
                ToolOut toolOut = t.invoke(user, functionCall);
                if(toolOut == null){
                    return onFunctionCallException(user, t, new FunctionCallException("Function call "+ t.getFunction().getName() + " returns null!"));
                }
                return onAssistantFunctionCall(user, functionCall, toolOut);
            }catch(Exception e){
                return onFunctionCallException(user, t, e);
            }
        }
        
        return onFunctionCallException(user, null, new FunctionCallException("Function " + functionCall.getName() + " does not exist."));
    }

}
