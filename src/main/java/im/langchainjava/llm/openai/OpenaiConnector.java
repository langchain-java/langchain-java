package im.langchainjava.llm.openai;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import lombok.Data;

@RegisterRestClient(baseUri = "https://api.openai.com/v1")
public interface OpenaiConnector {

    @Data
    static class Token{
        String value;
    }

    Token token = new Token();

    default OpenaiConnector setToken(String value){
        token.setValue(value);
        return this;
    }

    default String getToken() {
        return "Bearer " + token.getValue() ;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/chat/completions")
    @ClientHeaderParam(name = "Authorization", value = "{getToken}")
    ChatCompletionResult chatCompletion(ChatCompletionRequest request);

}