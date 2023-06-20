package im.langchainjava.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.agent.AsyncAgent.TriggerInput;
import im.langchainjava.parser.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public interface Tool {

    public static String KEY_OBSERVATION = "Observation";
    public static String KEY_THOUGHT = "Thought";
    
    public String getToolName();
    public String getToolDescription();
    public String getToolInputFormat();
    public ToolOut invoke(String user, Action<?> action);
    public void onClearedMemory(String user);
    default public String formatToolUsage(){
        return getToolName() + ": \t" + getToolDescription() + " " + getToolInputFormat();  
    }

    public static interface ToolOut extends Function<Void,Boolean>{
        public ToolOut handlerForKey(String key, Function<TriggerInput, Void> fun);
    }

    public static class ToolException extends RuntimeException{
        public ToolException(String log){
            super(log);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class ToolOutHandler{
        String key;
        String message;
        public Function<TriggerInput,Void> func;
    }

    public static class ToolOuts{
        final boolean next;
        final String user;
        final Map<String ,ToolOutHandler> handlerMap;
        final List<ToolOutHandler> handlers;

        public static ToolOuts of(String user, boolean next){
            return new ToolOuts(user, next);
        }
        
        public ToolOuts(String user, boolean next){
            this.user = user;
            this.handlers = new ArrayList<>();
            this.handlerMap = new HashMap<>();
            this.next = next;
        }

        public ToolOuts message(String key, String message){
            if(handlerMap.containsKey(key)){
                throw new ToolException("Duplcated handler for key " + key);
            }
            ToolOutHandler handler = new ToolOutHandler();
            handler.setKey(key);
            handler.setMessage(message);
            this.handlers.add(handler);
            this.handlerMap.put(key, handler);
            return this;
        } 
        
        public SyncToolOut sync(){
            return new SyncToolOut(this.user, this.handlers, this.handlerMap, this.next);
        }

        public AsyncToolOut async(){
            return new AsyncToolOut(this.user, this.handlers, this.handlerMap, this.next);
        }
    }

    public static class SyncToolOut implements ToolOut{

        final String user;
        final Map<String ,ToolOutHandler> handlerMap;
        final List<ToolOutHandler> handlers;
        final boolean next;

        public SyncToolOut(String user, List<ToolOutHandler> handlers, Map<String, ToolOutHandler> handlerMap, boolean next){
            this.user = user;
            this.handlerMap = handlerMap;
            this.handlers = handlers;
            this.next = next;
        }

        @Override
        public ToolOut handlerForKey(String key, Function<TriggerInput, Void> handler){
            ToolOutHandler h = null;
            if(!handlerMap.containsKey(key)){
                return this;
            }
            h = handlerMap.get(key);
            h.func = handler;
            return this;
        }

        @Override
        public Boolean apply(Void t) {

            if(this.handlers == null || this.handlers.isEmpty()){
                throw new ToolException("Please add a handler to the SyncToolOut. You may use ToolOuts.message(key, message).sync() to do that.");
            }

            runHandlers(user, handlers);
            
            return !this.next;
        }
    }

    static void runHandlers(String user, List<ToolOutHandler> handlers){
        runHandlers(user, handlers, null);
    }

    static void runHandlers(String user, List<ToolOutHandler> handlers, TriggerInput in){
        for(ToolOutHandler h : handlers){
            if(h == null){
                continue;
            }
            Function<TriggerInput, Void> fun = h.getFunc();
            if(fun == null){
                continue;
            }

            TriggerInput input = in;
            if(in == null){
                String message = h.getMessage();
                input = new TriggerInput(user, message);
            }

            fun.apply(input);
        }
    }

    public static class AsyncToolOut extends SyncToolOut{

        public AsyncToolOut(String user, List<ToolOutHandler> handlers, Map<String, ToolOutHandler> handlerMap, boolean next) {
            super(user, handlers, handlerMap, next);
        }

        @Override
        public Boolean apply(Void t) {
            return true;
        }

        public boolean applyLater(TriggerInput input){
            String u = input.getUser();
            if(u == null){
                throw new ToolException("The input user is null.");
            }
            if(this.user == null){
                throw new ToolException("Always use the constructor new AsyncToolOut(user) to init an AsyncToolOut instance with user id.");
            }
            if(!this.user.equals(u)){
                throw new ToolException("The input user " + u + " is not the applied user " + this.user);
            }
            
            if(this.handlers == null || this.handlers.isEmpty() || this.handlers.get(0) == null){
                throw new ToolException("Please add a handler to the SyncToolOut. You may use ToolOuts.message(key, message).sync() to do that.");
            }

            runHandlers(u, handlers, input);
            return !this.next;
        }
    }

}
