package im.langchainjava.embedding.openai;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import lombok.Data;

@RegisterRestClient(baseUri = "https://api.openai.com/v1")
public interface OpenaiEmbeddingConnector {

    @Data
    static class Token{
        String value;
    }

    Token token = new Token();

    default OpenaiEmbeddingConnector setToken(String value){
        token.setValue(value);
        return this;
    }

    default String getToken() {
        return "Bearer " + token.getValue() ;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/embeddings")
    @ClientHeaderParam(name = "Authorization", value = "{getToken}")
    EmbeddingResult embeddings(EmbeddingRequest request);


}