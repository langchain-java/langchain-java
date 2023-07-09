package im.langchainjava.agent;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FunctionCallAgent extends CommandAgent{

    public FunctionCallAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser c){
        super(llm, prompt, memory, c);
    }

    public abstract boolean onFunctionCall(String user, FunctionCall functionCall, String content);

    public abstract boolean onMessage(String user, String content);

    public abstract boolean onFunctionCallException(String user, Tool t, Exception e);

    @Override
    public boolean onAiResponse(String user, ChatMessage response){

        if(response.getFunctionCall() != null){
            try{
                FunctionCall call = response.getFunctionCall();
                String rawArguments = call.getArguments();
                Map<String, JsonNode> params = null;
                if(!StringUtil.isNullOrEmpty(rawArguments)){
                    params = JsonUtils.toMapOfJsonNode(rawArguments);
                    if(params == null){
                        return onFunctionCallException(user, null, new FunctionCallException("Could not parse parameters for function " + call.getName() + "."));
                    }
                }
                call.setParsedArguments(params);
                return onFunctionCall(user, call, response.getContent());
            }catch(Exception e){
                return onFunctionCallException(user, null, e);
            }
        }
        
        return onMessage(user, response.getContent());
    }
}
