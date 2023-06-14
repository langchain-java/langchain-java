package im.langchainjava.parser;

import lombok.Data;

@Data
public class Completion<T> extends Action<T>{
    
    String message;

    public Completion(String name, T input, String message) {
        super(name, input);
        this.message = message;
    }
    
}
