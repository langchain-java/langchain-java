package im.langchainjava.llm.openai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;

public interface OpenaiConnector {

    ChatCompletionResult chatCompletion(ChatCompletionRequest request);

}