package im.langchainjava.agent;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.parser.ChatResponseParser;
import im.langchainjava.prompt.ChatPromptProvider;

public abstract class MemoryAgent extends StructuredAiMessageAgent{

    private static int MAX_ROUNDS = 20;

    public MemoryAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, ChatResponseParser<?> p, CommandParser c) {
        super(llm, prompt, memory, p, c);
    }

    public abstract boolean onAssistantResponse(String user, Action<?> action);

    public abstract boolean onUnexpactedAssistantResponse(String user, String raw); 

    public abstract void onInternalError(String user, String raw, String errorMessage); 

    public abstract void onMaxRound(String user);

    @Override
    public boolean onParsedAiResponse(String user, Action<?> response, String raw) {
        if(memoryProvider.incrRoundAndGet(user) >= MAX_ROUNDS){
            onMaxRound(user);
            return true;
        }
        memoryProvider.onAssistantResponsed(user, raw);
        memoryProvider.onReceiveAssisMessage(user, raw);
        boolean assisResp = onAssistantResponse(user, response); 
        return assisResp;
    }

    @Override
    public void onParseError(String user, String raw) {
        onInternalError(user, raw, "Ai response parser is null!");
    }

    @Override
    public boolean onParseFailed(String user, String raw) {
        if(parser.getEnforceStructurePrompt()!=null){
            memoryProvider.addEndingMessage(user, parser.getEnforceStructurePrompt());
        }
        return false;
    }
}
