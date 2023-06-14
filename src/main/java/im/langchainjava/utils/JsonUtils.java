package im.langchainjava.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonUtils {
    public static JsonMapper mapper = new JsonMapper();

    public static Map<String,String> toMapOf(String json){
        try {
            return mapper.readerForMapOf(String.class).readValue(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fromMap(Map<String,String> map){
        try {
            return mapper.writerFor(new TypeReference<Map<String,String>>(){}).writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T toObject(String json, Class<T> clazz){
        try {
            return mapper.readerFor(clazz).readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fromList(List<?> list){
        try {
            return mapper.writerFor(new TypeReference<List<?>>(){}).writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] fromMapToBytes(Map<String,String> map){
        try {
            return mapper.writerFor(new TypeReference<Map<String,String>>(){}).writeValueAsBytes(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
