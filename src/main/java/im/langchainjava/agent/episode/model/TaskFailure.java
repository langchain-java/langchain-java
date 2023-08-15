package im.langchainjava.agent.episode.model;

import im.langchainjava.agent.exception.FunctionCallException;
import lombok.Data;

@Data
public class TaskFailure {
    String message;
    Exception e;

    public TaskFailure(String message){
        this.message = message;
        this.e = new FunctionCallException(message);
    }

    public TaskFailure(String message, Exception e){
        this.message = message;
        this.e = e;
    }

    public TaskFailure(Exception e){
        this.e = e;
        this.message = e.getMessage();
    }
}
