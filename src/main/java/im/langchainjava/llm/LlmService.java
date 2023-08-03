package im.langchainjava.llm;

import java.util.List;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;

public interface LlmService {

    public static String ROLE_SYSTEM="system";
    public static String ROLE_USER="user";
    public static String ROLE_ASSIS="assistant";
    public static String ROLE_FUNC="function";

    public ChatMessage chatCompletion(String user, List<ChatMessage> messages, List<Function> functions, FunctionCall call, LlmErrorHandler errorHandler);
}
