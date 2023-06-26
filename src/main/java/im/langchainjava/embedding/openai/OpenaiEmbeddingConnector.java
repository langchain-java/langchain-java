package im.langchainjava.embedding.openai;

import im.langchainjava.embedding.entity.EmbeddingRequest;
import im.langchainjava.embedding.entity.EmbeddingResult;

public interface OpenaiEmbeddingConnector {

    EmbeddingResult embeddings(EmbeddingRequest request);
}