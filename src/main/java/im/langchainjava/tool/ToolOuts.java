package im.langchainjava.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.tool.ControllorToolOut.Action;

public class ToolOuts{
    final String user;
    final Action action;
    final List<String> messageKeys;
    final Map<String, String> messages;

    public static ToolOuts of(String user){
        return new ToolOuts(user);
    }

    public static ToolOuts of(String user, Action act){
        return new ToolOuts(user, act);
    }
    
    public ToolOuts(String user){
        this.user = user;
        this.action = null;
        this.messages = new HashMap<>();
        this.messageKeys = new ArrayList<>();
    }

    public ToolOuts(String user, Action act){
        this.user = user;
        this.action = act;
        this.messages = new HashMap<>();
        this.messageKeys = new ArrayList<>();
    }

    public ToolOuts message(String key, String message){
        if(this.messages.containsKey(key)){
            throw new ToolException("Duplcated message key: " + key);
        }
        this.messageKeys.add(key);
        this.messages.put(key, message);
        return this;
    } 

    
    public ToolOut get(){
        List<ToolOutHandler> handlers = new ArrayList<>();
        Map<String, ToolOutHandler> handlerMap = new HashMap<>();
        for(String key : this.messageKeys){
            ToolOutHandler handler = new ToolOutHandler();
            String message = this.messages.get(key);
            handler.setKey(key);
            handler.setMessage(message);
            handlers.add(handler);
            handlerMap.put(key, handler);
        }
        if(this.action == null){
            return new AgentToolOut(this.user, handlers, handlerMap);
        }else{
            return new ControllorToolOut(user, handlers, handlerMap, action);
        }
    }

}
