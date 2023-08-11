package im.langchainjava.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ChatMemory{

    List<ChatMessage> history;
    Map<String,Object> context;
    long lastActiveTimestamp = 0L;
    long index = 0L;
    int round = 0;
    int functionCallNum = 0;
    BlockingQueue<ChatMessage> pending;
    ArrayList<ChatMessage> ending;

    public ChatMemory(){
        long now = System.currentTimeMillis();
        history = new ArrayList<>();
        lastActiveTimestamp = now;
        context = new HashMap<>();
        round = 0;
        index = now;
        pending = new LinkedBlockingQueue<>();
        this.ending = new ArrayList<>();
    }

    public int countMessageForRole(String role){
        int count = 0;
        for(ChatMessage msg : this.getHistory()){
            if(msg.getRole().equals(role)){
                count ++;
            }
        }
        return count;
    }

    public static ChatMemory of(){
        return new ChatMemory();
    }

    public Object getContext(String key){
        return context.getOrDefault(key, null);
    }

    public Object getContext(String key, Object deft){
        context.putIfAbsent(key, deft);
        return context.get(key);
    }

    public void setContext(String key, Object value){
        context.put(key, value);
        updateTimestamp();
    }

    public void clear(){
        history.clear();
        pending.clear();
        ending.clear();
        this.context.clear();
        this.round = 0;
        this.functionCallNum = 0;
        updateTimestamp();
    }

    private void updateTimestamp(){
        this.setLastActiveTimestamp(System.currentTimeMillis());
    }

    public void addHistMessageForRole(String role, String message){
        this.history.add(new ChatMessage(role, message));   
        updateTimestamp();
    }

    public void addPendingMessageForRole(String role, String message){
        addPendingMessageForRole(role, message, null);
    }

    public void addPendingMessageForRole(String role, String message, String name){
        addPendingMessageForRole(role, message, null, name);
    }

    public void addPendingMessageForRole(String role, String message, FunctionCall functionCall, String name){
        this.pending.add(new ChatMessage(role, message, name, functionCall));   
        updateTimestamp();
    }

    
    public void addPendingMessage(ChatMessage message){
        if(message != null){
            this.pending.add(message);   
            updateTimestamp();
        }
    }


    public void addEndingMessageForRole(String role, String message){
        this.ending.add(new ChatMessage(role, message));   
        updateTimestamp();
    }

    public void addHistMessages(List<ChatMessage> messages){
        this.history.addAll(messages);
    }

    public List<ChatMessage> drainPendingMessages(){
        List<ChatMessage> retList = new ArrayList<>(this.pending.size());
        ChatMessage msg = this.pending.poll();
        while(msg != null){
            retList.add(msg);
            msg = this.pending.poll();
        }
        return retList;
    }

    public void drainPendingMessagesToHist(boolean truncate, String role){
        ChatMessage msg = this.pending.poll();
        while(msg != null){
            if(truncate && msg.getRole().equals(role)){
                msg.setContent("<truncated>");
            }
            history.add(msg);
            msg = this.pending.poll();
        }
    }

    public List<ChatMessage> getPendingUserMessage(){
        List<ChatMessage> messages = new ArrayList<>();
        for(ChatMessage m : pending){
            messages.add(m);
        } 
        return messages;
    }

    public int incrRoundAndGet(){
        this.round ++;
        return this.round;
    }

    public int incrFunctionCallAndGet(){
        this.functionCallNum ++;
        return this.functionCallNum;
    }
}
