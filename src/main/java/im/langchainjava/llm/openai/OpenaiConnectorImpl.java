package im.langchainjava.llm.openai;

import java.util.HashMap;
import java.util.Map;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;

import im.langchainjava.utils.RestUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpenaiConnectorImpl implements OpenaiConnector{
    
    private static String url = "https://api.openai.com/v1/chat/completions";

    String token;

    String getToken(){
        return "Bearer " + token;
    }

    @Override
    public ChatCompletionResult chatCompletion(ChatCompletionRequest request){
        Map<String, String> headers = new HashMap<String,String>();
        headers.put("Authorization", getToken());
        return RestUtil.post(url, request, headers, ChatCompletionResult.class);
    }

    
    
}
