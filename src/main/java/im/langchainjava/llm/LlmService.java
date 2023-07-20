package im.langchainjava.llm;

import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;

public interface LlmService {
    public ChatMessage chatCompletion(String user, List<ChatMessage> messages, List<Function> functions, FunctionCall call, LlmErrorHandler errorHandler);
}
