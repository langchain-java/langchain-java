package im.langchainjava.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.utils.JsonUtils;

import static im.langchainjava.llm.LlmService.ROLE_ASSIS;
import static im.langchainjava.llm.LlmService.ROLE_FUNC;
import static im.langchainjava.llm.LlmService.ROLE_SYSTEM;
import static im.langchainjava.llm.LlmService.ROLE_USER;

public class BasicChatMemory implements ChatMemoryProvider{
    
    private Map<String, ChatMemory> chats = new ConcurrentHashMap<>();

    private ChatMemory getMemory(String user) {
        chats.putIfAbsent(user, ChatMemory.of());
        ChatMemory memory = chats.get(user);
        return memory;
    }

    @Override
    public List<ChatMessage> getPrompt(String user) {
        ChatMemory memory = getMemory(user);
        List<ChatMessage> hist = new ArrayList<>();
        if(memory != null && memory.getHistory() != null){
            hist.addAll(memory.getHistory());
        }
        if(memory!=null && memory.getPending()!= null){
            hist.addAll(memory.getPending());
        }
        hist.addAll(memory.getEnding());
        return hist; 
    }

    @Override
    public void clearHistory(String user){
        ChatMemory memory = getMemory(user);
        memory.clear();
    }

    @Override
    public void onReceiveUserMessage(String user, String message) {
        ChatMemory memory = getMemory(user);
        System.out.println(ROLE_USER+ ":\t" + message);
        memory.addPendingMessageForRole(ROLE_USER, message);
    }

    @Override
    public void setContextForUser(String user, String key, Object value) {
        ChatMemory m = getMemory(user);
        m.setContext(key, value);
    }

    @Override
    public Object getContextForUser(String user, String key, Object defaultValue) {
        return getMemory(user).getContext(key, defaultValue);
    }

    @Override
    public int countUserMessage(String user) {
        return getMemory(user).countMessageForRole(ROLE_ASSIS);
    }

    @Override
    public void onReceiveSystemMessage(String user, String message) {
        System.out.println(ROLE_SYSTEM+ ":\t" + message);
        getMemory(user).addPendingMessageForRole(ROLE_SYSTEM, message);
    }

    @Override
    public void onReceiveAssisMessage(String user, String message) {
        System.out.println(ROLE_ASSIS+ ":\t" + message);
        getMemory(user).addPendingMessageForRole(ROLE_ASSIS, message);
    }

    @Override
    public void onReceiveFunctionCall(String user, FunctionCall message){
        System.out.println(ROLE_ASSIS + ":\t" + JsonUtils.fromObject(message));
        getMemory(user).addPendingMessageForRole(ROLE_ASSIS, "none", message, user);
    }

    @Override
    public void onReceiveFunctionCallResult(String user, String message){
        System.out.println(ROLE_FUNC + ":\t" + message);
        getMemory(user).addPendingMessageForRole(ROLE_FUNC, message, user);
    }


    @Override
    public void onAssistantResponsed(String user) {
        getMemory(user).drainPendingMessagesToHist(false, ROLE_FUNC);
        getMemory(user).getEnding().clear();
    }

    @Override
    public void showMemory(String user) {
        ChatMemory memory = getMemory(user);
        System.out.println("-----------History:-------------");
        for(ChatMessage m : memory.getHistory()){
            System.out.println(m.getRole()+ ":\t" + m.getContent());
        }
        System.out.println("-----------Pending:-------------");
        for(ChatMessage m : memory.getPending()){
            System.out.println(m.getRole()+ ":\t" + m.getContent());
        }
    }

    @Override
    public List<ChatMessage> getPendingMessage(String user) {
        return new ArrayList<>(getMemory(user).getPending());
    }

    @Override
    public void drainPendingMessages(String user) {
        getMemory(user).drainPendingMessages();
    }

    @Override
    public void clearEndingMessage(String user) {
        getMemory(user).getEnding().clear();
    }

    @Override
    public void reset(String user) {
        ChatMemory m = getMemory(user);
        m.clear();
    }

    @Override
    public int getRound(String user) {
        return getMemory(user).getRound();
    }

    @Override
    public int incrRoundAndGet(String user) {
        return getMemory(user).incrRoundAndGet();
    }

    @Override
    public int getFunctionCallNum(String user){
        return getMemory(user).getFunctionCallNum();
    }

    @Override
    public int incrFunctionCallAndGet(String user){
        return getMemory(user).incrFunctionCallAndGet();
    }

}
