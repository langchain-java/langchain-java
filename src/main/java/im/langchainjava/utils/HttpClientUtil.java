package im.langchainjava.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
public class HttpClientUtil {

    private static String UTF8 = "utf-8";

    private static RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(35000)// 连接主机服务超时时间
            .setConnectionRequestTimeout(60000)// 请求超时时间
            .setSocketTimeout(60000)// 数据读取超时时间
            .build();

    public static <T> T get(String url, Class<T> clazz){
        return get(url, null, null, clazz);
    }

    public static <T> T get(String url, Map<String,String> params, Class<T> clazz){
        return get(url, params, null, clazz);
    }

    public static <T> T get(String url, Map<String,String> params, Map<String,String> headers, Class<T> clazz){
        URIBuilder uriBuilder;
        HttpGet httpGet;
        try {
            uriBuilder = new URIBuilder(url);
            if(params != null && !params.isEmpty()){
                for(Entry<String,String> param : params.entrySet()){
                    uriBuilder.addParameter(param.getKey(), param.getValue());
                }
            }
            httpGet = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        if(headers != null && !headers.isEmpty()){
            for(Entry<String,String> e : headers.entrySet()){
                httpGet.addHeader(e.getKey(), e.getValue());
            }
        }

        httpGet.setConfig(requestConfig);
        try(CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(httpGet)){
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String raw = EntityUtils.toString(entity);
            if(statusCode != HttpStatus.SC_OK){
                log.info("<" + statusCode + "> \n" + raw);
            }
            return JsonUtils.toObject(raw, clazz);
        }catch (IOException e) {
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
        HttpEntity entity;
        URIBuilder uriBuilder;
        HttpPost post;
        try {
            entity = new StringEntity(body, UTF8);
            uriBuilder = new URIBuilder(url);
            if(params != null && !params.isEmpty()){
                for(Entry<String,String> param : params.entrySet()){
                    uriBuilder.addParameter(param.getKey(), param.getValue());
                }
            }
            post = new HttpPost(uriBuilder.build());
            post.setEntity(entity);
            if(headers != null && !headers.isEmpty()){
                for(Entry<String,String> e : headers.entrySet()){
                    post.addHeader(e.getKey(), e.getValue());
                }
            }
            post.setConfig(requestConfig);
        }catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        try (CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
              CloseableHttpResponse response = closeableHttpClient.execute(post);
        ) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            String raw = EntityUtils.toString(responseEntity);
            if(statusCode != HttpStatus.SC_OK){
                log.info("<" + statusCode + "> \n" + raw);
            }
            return JsonUtils.toObject(raw, clazz);

        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
