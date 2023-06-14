package im.langchainjava.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.theokanning.openai.completion.chat.ChatMessage;

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
        this.pending.add(new ChatMessage(role, message));   
        updateTimestamp();
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

    private static int MAX_MEMORY_CHAT_MESSAGE_LENGTH = 250;

    public void drainPendingMessagesToHist(boolean truncate){
        ChatMessage msg = this.pending.poll();
        while(msg != null){
            if(truncate){
                msg.setContent(msg.getContent().substring(0, Math.min(msg.getContent().length(), MAX_MEMORY_CHAT_MESSAGE_LENGTH)));
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
}
