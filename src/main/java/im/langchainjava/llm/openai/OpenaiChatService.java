package im.langchainjava.llm.openai;

import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;

import im.langchainjava.llm.LlmService;

public class OpenaiChatService implements LlmService{

    String token;

    String model;

    double temperature;

    int maxTokens;

    int choiceNumber;

    String stop;

    OpenaiConnector openaiConnector;

    public OpenaiChatService(OpenaiConnector connector, String token, String model, double temperature, int maxTokens, int choiceNum, String stop){
        this.openaiConnector = connector;
        this.token = token;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.choiceNumber = choiceNum;
        this.stop = stop;
    }

    @Override
    public String chatCompletion(String user, List<ChatMessage> messages){
        List<String> stopWords = new ArrayList<>();
        stopWords.add(this.stop);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
            .model(this.model)
            .messages(messages)
            .temperature(this.temperature)
            .maxTokens(this.maxTokens)
            .user(user)
            .stop(stopWords)
            .n(this.choiceNumber)
            .build();
        ChatCompletionResult result = openaiConnector.setToken(this.token).chatCompletion(chatCompletionRequest);
        List<ChatCompletionChoice> choices = result.getChoices();
        if(choices.isEmpty()){
            return "";
        }
        String response = choices.get(0).getMessage().getContent();
        return response;
    }
    

}
