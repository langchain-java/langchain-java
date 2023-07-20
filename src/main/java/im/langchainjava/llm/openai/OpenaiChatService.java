package im.langchainjava.llm.openai;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.agent.exception.AiResponseException;
import im.langchainjava.llm.LlmErrorHandler;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatCompletionChoice;
import im.langchainjava.llm.entity.ChatCompletionRequest;
import im.langchainjava.llm.entity.ChatCompletionResult;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenaiChatService implements LlmService{

    private static String CHAT_COMPLETION_FAILURE_CODE_TOKEN_LENGTH_EXCEED = "context_length_exceeded";
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
    public ChatMessage chatCompletion(String user, List<ChatMessage> messages, List<Function> functions, FunctionCall functionCall, LlmErrorHandler handler){
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

        if(functionCall != null){
            chatCompletionRequestBuilder.functionCall(functionCall);
        }

        ChatCompletionRequest chatCompletionRequest = chatCompletionRequestBuilder.build();
        ChatCompletionResult result = openaiConnector.chatCompletion(chatCompletionRequest);

        if(result == null){
            log.info("Openai service returns null!");
            if(handler != null){
                handler.onAiException(user, new AiResponseException("Openai service returns null."));
            }
            return null;
        }

        if(result.getError() != null){
            if(result.getError().getCode() != null 
                    && result.getError().getCode().equals(CHAT_COMPLETION_FAILURE_CODE_TOKEN_LENGTH_EXCEED)){
                log.info("Openai max token exceeded!");
                if(handler != null){
                    handler.onMaxTokenExceeded(user);
                }
                return null;
            }
            if(handler != null){
                handler.onAiException(user, new AiResponseException(result.getError().getMessage()));
            }
            return null;
        }

        List<ChatCompletionChoice> choices = result.getChoices();
        if(choices == null || choices.isEmpty()){
            log.info("Openai service returns empty choices!");
            if(handler != null){
                handler.onAiException(user, new AiResponseException("Openai service returns empty choices."));
            }
            return null;
        }

        return choices.get(0).getMessage();
    }

}
