package im.langchainjava.tool;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AgentToolOut implements ToolOut{
    final String user;
    final Map<String ,ToolOutHandler> handlerMap;
    final List<ToolOutHandler> handlers;

    public AgentToolOut(String user, List<ToolOutHandler> handlers, Map<String, ToolOutHandler> handlerMap){
        this.user = user;
        this.handlerMap = handlerMap;
        this.handlers = handlers;
    }


    @Override
    public ToolOut handlerForKey(String key, Function<FunctionMessage, Void> handler){
        ToolOutHandler h = null;
        if(!handlerMap.containsKey(key)){
            return this;
        }
        h = handlerMap.get(key);
        h.func = handler;
        return this;
    }

    @Override
    public void run() {

        if(this.handlers == null || this.handlers.isEmpty()){
            return;
        }

        runHandlers(user, handlers);
        
    }


    static void runHandlers(String user, List<ToolOutHandler> handlers){
        runHandlers(user, handlers, null);
    }

    static void runHandlers(String user, List<ToolOutHandler> handlers, FunctionMessage in){
        for(ToolOutHandler h : handlers){
            if(h == null){
                continue;
            }
            Function<FunctionMessage, Void> fun = h.getFunc();
            if(fun == null){
                continue;
            }

            FunctionMessage input = in;
            if(in == null){
                String message = h.getMessage();
                input = new FunctionMessage(user, message);
            }

            fun.apply(input);
        }
    }


    @Override
    public String getMessageForKey(String key) {
        if(this.handlerMap.get(key) != null){
            this.handlerMap.get(key).getMessage();
        }
        return null;
    }
}
