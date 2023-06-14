package im.langchainjava.parser;

import lombok.Data;

@Data
public class Failure<T> extends Action<T>{

    String message;

    public Failure(String name, T input, String message) {
        super(name, input);
        this.message = message;
    }
    
}
