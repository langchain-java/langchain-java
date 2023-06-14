package im.langchainjava.parser;

public class AiParseException extends RuntimeException {

    public AiParseException(String msg){
        super(msg);
    }

    public AiParseException(String msg, Exception e){
        super(msg, e);
    }
}
