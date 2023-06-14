package im.langchainjava.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Action<T> {
    String name;
    String intention;
    String thought;
    String question;
    T input;

    public Action(String name, T input){
        this.name = name;
        this.input = input;
        this.intention = null;  
        this.thought = null;
        this.question = null;
    }
}
