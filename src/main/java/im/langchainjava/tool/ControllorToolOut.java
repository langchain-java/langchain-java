package im.langchainjava.tool;

import java.util.List;
import java.util.Map;

import lombok.Getter;

public class ControllorToolOut extends AgentToolOut{

    @Getter
    final Status status;

    public ControllorToolOut(String user, List<ToolOutHandler> handlers, Map<String, ToolOutHandler> handlerMap, Status status){
        super(user, handlers, handlerMap);
        this.status = status;
    }

    public static enum Status{ 
        success,
        failed,
        wait,
        halt,
        next;
    }
}
