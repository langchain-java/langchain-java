package im.langchainjava.llm.openai;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatCompletionChoice;
import im.langchainjava.llm.entity.ChatCompletionFailure;
import im.langchainjava.llm.entity.ChatCompletionRequest;
import im.langchainjava.llm.entity.ChatCompletionResult;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;

public class OpenaiChatService implements LlmService{

    public static String MODEL_GPT_3_5_TURBO_0613 = "gpt-3.5-turbo-0613";

    public static String MODEL_GPT_3_5_TURBO = "gpt-3.5-turbo";

    String token;

    String model;

    double temperature;

    int maxTokens;

    int choiceNumber;

    String stop;

    OpenaiConnector openaiConnector;

    public OpenaiChatService(String token, String model, double temperature, int maxTokens, int choiceNum, String stop){
        this.openaiConnector = new OpenaiConnectorImpl(token);
        this.token = token;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.choiceNumber = choiceNum;
        this.stop = stop;
    }

    @Override
    public ChatMessage chatCompletion(String user, List<ChatMessage> messages, List<Function> functions, java.util.function.Function<ChatCompletionFailure, Void> errorHandler){
        List<String> stopWords = null;
        if(this.stop != null){
           stopWords = new ArrayList<>();
           stopWords.add(this.stop);
        }
        ChatCompletionRequest.ChatCompletionRequestBuilder chatCompletionRequestBuilder = ChatCompletionRequest.builder()
            .model(this.model)
            .messages(messages)
            .temperature(this.temperature)
            .maxTokens(this.maxTokens)
            .user(user)
            .stop(stopWords)
            .n(this.choiceNumber);
        if(functions != null && !functions.isEmpty()){
            chatCompletionRequestBuilder.functions(functions);
        }

        ChatCompletionRequest chatCompletionRequest = chatCompletionRequestBuilder.build();
        ChatCompletionResult result = openaiConnector.chatCompletion(chatCompletionRequest);

        if(result == null){
            return null;
        }

        if(result.getError() != null){
            if(errorHandler != null){
                errorHandler.apply(result.getError());
            }
            return null;
        }
        List<ChatCompletionChoice> choices = result.getChoices();
        if(choices == null || choices.isEmpty()){
            return null;
        }
        return choices.get(0).getMessage();
    }

}
