package im.langchainjava.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.theokanning.openai.completion.chat.ChatMessage;

import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static im.langchainjava.memory.BasicChatMemory.ROLE_USER;

@Slf4j
@Getter
@Setter
public abstract class AsyncAgent {

    private static String MEMORY_KEY_RETRY = "retry";
    private static int MAX_RETRY = 3;

    ChatPromptProvider promptProvider;

    ChatMemoryProvider memoryProvider;

    LlmService llm;

    private List<Function<TriggerInput, Void>> triggers = new ArrayList<>();

    private LinkedBlockingQueue<String> waiting = new LinkedBlockingQueue<>(); 

    private LinkedBlockingQueue<String> processing = new LinkedBlockingQueue<>(); 

    public void registerTrigger(Function<TriggerInput,Void> function){
        this.triggers.add(function);
    }
    
    public AsyncAgent(ChatPromptProvider prompt, ChatMemoryProvider memory, LlmService llm){
        this.promptProvider = prompt;
        this.memoryProvider = memory; 
        this.llm = llm;
        run();
    }

    public abstract boolean onUserMessage(String user, String text);

    public abstract boolean onAiResponse(String user, String response);

    public abstract void onUserMessageAtBusyTime(String user, String text);

    public abstract void onAiException(String user, Exception e);

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
        String resp = null;
        do{
            List<ChatMessage> prompt = promptProvider.getPrompt(user);
            StringBuilder sb = new StringBuilder();
            List<ChatMessage> messages = memoryProvider.getPendingMessage(user);
            log.info("The pending message in memory # is:" + messages.size());
            if(messages.isEmpty()){
                break;
            }
            for(ChatMessage m : messages){
                if(m.getRole().equals(ROLE_USER)){
                    sb.append(m.getContent()).append("\n");
                }
            }
            String message = sb.toString();
            for(Function<TriggerInput,Void> trigger : this.triggers){
                trigger.apply(new TriggerInput(user, message));
            }
            resp = llm.chatCompletion(user, prompt); 
        }while(!onAiResponse(user,resp));
    }

    public void showMessages(List<ChatMessage> messages){
        System.out.println("-----------Prompt:-------------");
        for(ChatMessage m : messages){
            System.out.println(m.getRole()+ ":\t" + m.getContent());
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

    @Getter
    @AllArgsConstructor
    public static class TriggerInput{
        String user;
        String message;
    }

}
