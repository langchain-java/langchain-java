package im.langchainjava.agent;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.parser.ChatResponseParser;
import im.langchainjava.prompt.ChatPromptProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StructuredAiMessageAgent extends CommandAgent{

    ChatResponseParser<?> parser;

    public StructuredAiMessageAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, ChatResponseParser<?> p, CommandParser c){
        super(llm, prompt, memory, c);
        this.parser = p;
    }

    public abstract boolean onParsedAiResponse(String user, Action<?> response, String raw);

    public abstract void onParseError(String user, String raw);

    public abstract boolean onParseFailed(String user, String raw);

    @Override
    public boolean onAiResponse(String user, String response){
        if(this.parser == null){
            log.warn("Ai response parser is null! Will always output the raw string.");
            onParseError(user, response);
            return true;
        }
        
        Action<?> action = null;
        try{
            action = parser.parse(response);
            log.info("parsed action:" + action.getName() + ":" + action.getInput());
        }catch(Exception e){
            action = null;
        }
        if(action == null){
            return onParseFailed(user, response);
        }

        return onParsedAiResponse(user, action, response);
    }


}
