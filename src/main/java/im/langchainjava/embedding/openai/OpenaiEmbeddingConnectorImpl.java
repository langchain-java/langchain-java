package im.langchainjava.embedding.openai;

import java.util.HashMap;
import java.util.Map;

import im.langchainjava.embedding.entity.EmbeddingRequest;
import im.langchainjava.embedding.entity.EmbeddingResult;
import im.langchainjava.utils.HttpClientUtil;
// import im.langchainjava.utils.RestUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpenaiEmbeddingConnectorImpl implements OpenaiEmbeddingConnector{

    private static String url = "https://api.openai.com/v1/embeddings";

    String token;

    String getToken(){
        return "Bearer " + token;
    }

    @Override
    public EmbeddingResult embeddings(EmbeddingRequest request) {
        Map<String, String> headers = new HashMap<String,String>();
        headers.put("Authorization", getToken());
        return HttpClientUtil.post(url, request, headers, EmbeddingResult.class);
    }
    
}
