package im.langchainjava.agent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import im.langchainjava.llm.LlmErrorHandler;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public abstract class AsyncAgent implements LlmErrorHandler{

    private static String MEMORY_KEY_RETRY = "retry";
    private static int MAX_RETRY = 3;

    ChatPromptProvider promptProvider;

    ChatMemoryProvider memoryProvider;

    LlmService llm;

    boolean showPrompt = false;

    private LinkedBlockingQueue<String> waiting = new LinkedBlockingQueue<>(); 

    private LinkedBlockingQueue<String> processing = new LinkedBlockingQueue<>(); 

    public AsyncAgent(ChatPromptProvider prompt, ChatMemoryProvider memory, LlmService llm){
        this.promptProvider = prompt;
        this.memoryProvider = memory; 
        this.llm = llm;
        run();
    }

    public abstract boolean onUserMessage(String user, String text);
    public abstract void onUserMessageAtBusyTime(String user, String text);

    public abstract boolean onAiResponse(String user, ChatMessage response, boolean isUserTurn);
    public abstract boolean onInvokingAi(String user, boolean isUserTurn);

    public void chat(String user, String message){
        if(waiting.contains(user) || processing.contains(user)){
            onUserMessageAtBusyTime(user, message);
            return;
        }

        if(!onUserMessage(user, message)){
            return;
        }

        memoryProvider.onReceiveUserMessage(user, message);

        this.waiting.offer(user);
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1000);
    public void run(){
        log.info("scheduling...");
        scheduledExecutorService.scheduleAtFixedRate(()->{
            if(!waiting.isEmpty()){
                log.info("waiting:" + waiting.size());
            }
            String user = waiting.poll();
            if(user != null){
                log.info("processing message for user:" + user);
                processing.offer(user);
                while(true){
                    try{
                        doChat(user);
                        resetRetry(user);
                        break;
                    }catch(Exception e){
                        log.error("Error occurs while completion.");
                        e.printStackTrace();
                        incRetry(user);
                        if(getRetry(user) >= MAX_RETRY){
                            log.error("Max retry reached.");
                            onAiException(user, e);
                            break;
                        }
                    }
                }
                processing.remove(user);
                log.info("[done] processing message for user:" + user);
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS); 
    }

    private void doChat(String user){
        ChatMessage chatMessage = null;

        boolean isUserTurn = true;
        
        while(true){

            if(showPrompt){
                showMessages("agent", promptProvider.getPrompt(user));
            }

            if(!onInvokingAi(user, isUserTurn)){
                break;
            }

            chatMessage = llm.chatCompletion(user, promptProvider.getPrompt(user), promptProvider.getFunctions(user), promptProvider.getFunctionCall(user),  this);

            if(chatMessage == null){
                // all exceptions causing chatMessage == null are handled in chatCompletion. 
                // We simple do control logic here.
                break;
            }
            
            boolean next = onAiResponse(user, chatMessage, isUserTurn);
            
            isUserTurn = false;
            
            if(!next){
                break;
            }
        }
    }

    public void showMessages(String id, List<ChatMessage> messages){
        System.out.println("-----------Prompt:-------------" + id);
        for(ChatMessage m : messages){
            String message = m.getContent();
            if(m.getFunctionCall() != null){
                message = "FunctionCall: " + JsonUtils.fromObject(m.getFunctionCall());
            }
            System.out.println(m.getRole()+ ":\t" + message);
        }
        System.out.println("===========END of Prompt=======");
    }

    private int getRetry(String user){
        return (Integer) memoryProvider.getContextForUser(user, MEMORY_KEY_RETRY, Integer.valueOf(0));
    }

    private void resetRetry(String user){
        memoryProvider.setContextForUser(user, MEMORY_KEY_RETRY, Integer.valueOf(0));
    }

    private void incRetry(String user){
        int retry = getRetry(user) + 1;
        memoryProvider.setContextForUser(user, MEMORY_KEY_RETRY, Integer.valueOf(retry));
    }

    public void setContext(String user, String key, Object value){
        memoryProvider.setContextForUser(user, key, value);
    }

    public Object getContext(String user, String key){
        return memoryProvider.getContextForUser(user, key, null);
    }

}
