package im.langchainjava.llm;

import java.util.List;

import im.langchainjava.llm.entity.ChatCompletionFailure;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;

public interface LlmService {
    public ChatMessage chatCompletion(String user, List<ChatMessage> messages, List<Function> functions, java.util.function.Function<ChatCompletionFailure, Void> errorHandler);
}
