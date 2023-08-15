package im.langchainjava.tool;

import java.util.Map;

import lombok.Getter;

public class ControllorToolOut implements ToolOut{

    @Getter
    final String user;

    @Getter
    final Status status;

    @Getter
    // final Map<String, String> output;
    final String output;

    @Getter
    final String error;

    public ControllorToolOut(String user, Status status, String output, String error){
        this.status = status;
        this.user = user;
        this.output = output;
        this.error = error;
    }

    public static enum Status{ 
        success,
        failed,
        wait,
        halt,
        next;
    }
}
