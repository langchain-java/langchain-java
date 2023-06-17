package im.langchainjava.embedding.openai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;

import im.langchainjava.embedding.EmbeddingService;

public class OpenaiEmbeddingService implements EmbeddingService{

    String model;

    String token;

    OpenaiEmbeddingConnector openaiConnector;

    public OpenaiEmbeddingService(String token, String model){
        this.openaiConnector = new OpenaiEmbeddingConnectorImpl(token);
        this.token = token;
        this.model = model;
    }

    private List<List<Double>> embedding(String user, List<String> text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model(this.model)
                .input(text)
                .user(user)
                .build();
        EmbeddingResult result = openaiConnector.embeddings(request);
        if(result != null && result.getData()!= null && !result.getData().isEmpty()){
            List<List<Double>> embeddings = new ArrayList<>();
            for(Embedding embd: result.getData()){
                embeddings.add(embd.getEmbedding());
            }
            return embeddings;
        }
        return null;
    }

    @Override
    public List<float[]> embededDocuments(String user, List<String> document) {
        List<float[]> retEmbedding = new ArrayList<>();

        List<List<Double>> embeddings = embedding(user, document);
        if(embeddings == null){
            return null;
        }

        for(List<Double> em : embeddings){
            int size = em.size();
            float[] vector = new float[size];
            for(int i = 0; i< size; i++){
                vector[i] = em.get(i).floatValue();
            }
            retEmbedding.add(vector);
        }
        return retEmbedding;
    }

    @Override
    public float[] embededQuery(String user, String query) {
        List<List<Double>> embeddings = embedding(user, Collections.singletonList(query));
        if(embeddings == null){
            return null;
        }
        if(embeddings.isEmpty()){
            return null;
        }

        if(embeddings.get(0).isEmpty()){
            return null;
        }

        int size = embeddings.get(0).size();
        float[] vector = new float[size];
        for(int i = 0; i< size; i++){
            vector[i] = embeddings.get(0).get(i).floatValue();
        }
        return vector;
    }
    
}
