package im.langchainjava.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RestUtil {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static <T> T get(String url, Class<T> clazz){
        return get(url, null, null, clazz);
    }

    public static <T> T get(String url, Map<String,String> params, Class<T> clazz){
        return get(url, params, null, clazz);
    }

    public static <T> T get(String url, Map<String,String> params, Map<String,String> headers, Class<T> clazz){

        HttpUrl.Builder queryUrlBuilder = HttpUrl.get(url).newBuilder();
        if(params != null && !params.isEmpty()){
            for(Entry<String,String> param : params.entrySet()){
                queryUrlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        Request.Builder builder = new Request.Builder();
        if(headers != null && !headers.isEmpty()){
            for(Entry<String,String> e : headers.entrySet()){
                builder.addHeader(e.getKey(), e.getValue());
            }
        }
        Request getRequest = builder.url(queryUrlBuilder.build()).get().build();

        try {
            Response response = client.newCall(getRequest).execute();
            if(response.isSuccessful()){
                String raw = response.body().string();
                return JsonUtils.toObject(raw, clazz);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <U,T> T post(String url, Map<String,String> params, U body, Class<T> clazz){
        return post(url, params, JsonUtils.fromObject(body), null, clazz);
    }

    public static <U,T> T post(String url, U body, Class<T> clazz){
        return post(url, null, JsonUtils.fromObject(body), null, clazz);
    }

    public static <U,T> T post(String url, U body, Map<String, String> headers, Class<T> clazz){
        return post(url, null, JsonUtils.fromObject(body), headers, clazz);
    }

    public static <T> T post(String url, Map<String,String> params, String body, Map<String,String> headers, Class<T> clazz){
        try{

            HttpUrl.Builder queryUrlBuilder = HttpUrl.get(url).newBuilder();
            if(params != null && !params.isEmpty()){
                for(Entry<String,String> param : params.entrySet()){
                    queryUrlBuilder.addQueryParameter(param.getKey(), param.getValue());
                }
            }
    
            Request.Builder builder = new Request.Builder();
            if(headers != null && !headers.isEmpty()){
                for(Entry<String,String> e : headers.entrySet()){
                    builder.addHeader(e.getKey(), e.getValue());
                }
            }
    
            RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json"));
            
            Request getRequest = builder.url(queryUrlBuilder.build()).post(requestBody).build();
    
            Response response = client.newCall(getRequest).execute();
            if(response.isSuccessful()){
                String raw = response.body().string();
                return JsonUtils.toObject(raw, clazz);
            }else{
                log.info("<" + response.code() + ">" + response.body().string());
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
