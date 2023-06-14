package im.langchainjava.vectorstore;

import java.util.Map;

import lombok.Data;

@Data
public class Document {
    Map<String,String> meta;
    String content;
    float[] vector; 
}
