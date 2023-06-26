package im.langchainjava.llm.openai;

import im.langchainjava.llm.entity.ChatCompletionRequest;
import im.langchainjava.llm.entity.ChatCompletionResult;

public interface OpenaiConnector {

    ChatCompletionResult chatCompletion(ChatCompletionRequest request);

}