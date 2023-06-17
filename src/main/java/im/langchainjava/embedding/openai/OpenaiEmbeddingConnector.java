package im.langchainjava.embedding.openai;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;


public interface OpenaiEmbeddingConnector {

    EmbeddingResult embeddings(EmbeddingRequest request);
}