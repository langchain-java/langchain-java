package im.langchainjava.vectorstore;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface VectorStore {
    public List<Document> similaritySearch(String user, String text, int k);
    public void addDocuments(String user, List<String> docs, List<Map<String,String>> meta);
    public void loadFromFile(File file);
}
