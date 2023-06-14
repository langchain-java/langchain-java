package im.langchainjava.embedding;

import java.util.List;

public interface EmbeddingService {
    public List<float[]> embededDocuments(String user, List<String> document);
    public float[] embededQuery(String user, String query);
}
