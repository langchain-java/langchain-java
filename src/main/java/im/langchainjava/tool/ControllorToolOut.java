package im.langchainjava.tool;

import java.util.List;
import java.util.Map;

import lombok.Getter;

public class ControllorToolOut extends AgentToolOut{

    @Getter
    final Action action;

    public ControllorToolOut(String user, List<ToolOutHandler> handlers, Map<String, ToolOutHandler> handlerMap, Action action){
        super(user, handlers, handlerMap);
        this.action = action;
    }

    public static enum Action{ 
        waitUserInput,
        endConversation,
        next;
    }
}
