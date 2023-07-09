package im.langchainjava.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import im.langchainjava.agent.exception.AiResponseException;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatCompletionFailure;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    private static String CHAT_COMPLETION_FAILURE_CODE_TOKEN_LENGTH_EXCEED = "context_length_exceeded";

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

    public abstract boolean onAiResponse(String user, ChatMessage response);

    public abstract void onUserMessageAtBusyTime(String user, String text);

    public abstract void onAiException(String user, Exception e);

    public abstract void onMaxTokenExceeded(String user);

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
        
        do{
            ChatCompletionErrorHandler errorHandler = new ChatCompletionErrorHandler(user, null);
            List<ChatMessage> messages = memoryProvider.getPendingMessage(user);
            log.info("The pending message in memory # is:" + messages.size());
            if(messages.isEmpty()){
                break;
            }
            StringBuilder sb = new StringBuilder();
            for(ChatMessage m : messages){
                if(m.getRole().equals(ROLE_USER)){
                    sb.append(m.getContent()).append("\n");
                }
            }
            String message = sb.toString();
            for(Function<TriggerInput, Void> trigger : this.triggers){
                trigger.apply(new TriggerInput(user, message)); 
            }
            // chatMessage = llm.chatCompletion(user, promptProvider.getPrompt(user), promptProvider.getFunctions(user), errorHandler);
            ChatMessage level1Resp = llm.chatCompletion(user, promptProvider.getPrompt(user), null, errorHandler);
            log.info(level1Resp.getContent());
            chatMessage = llm.chatCompletion(user, promptProvider.getFunctionCallPrompt(user, level1Resp.getContent()), promptProvider.getFunctions(user), errorHandler);
            
            if(chatMessage == null){
                if(errorHandler.getFailure() != null){
                    if(errorHandler.getFailure().getCode() != null 
                            && errorHandler.getFailure().getCode().equals(CHAT_COMPLETION_FAILURE_CODE_TOKEN_LENGTH_EXCEED)){
                        log.info("Llm max token exceeded!");
                        onMaxTokenExceeded(user);
                        break;
                    }
                    onAiException(user, new AiResponseException(errorHandler.getFailure().getMessage()));
                    break;
                }
                log.info("Llm service returns null!");
                onAiException(user, new AiResponseException("Llm response nothing."));
                break;
            }
        }while(!onAiResponse(user, chatMessage));
    }

    @Data
    @AllArgsConstructor
    public class ChatCompletionErrorHandler implements java.util.function.Function<ChatCompletionFailure, Void>{
        String user;
        ChatCompletionFailure failure = null; 
        @Override
        public Void apply(ChatCompletionFailure f) {
            this.failure = f;
            return null;
        }
    }

    public void showMessages(List<ChatMessage> messages){
        System.out.println("-----------Prompt:-------------");
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

    @Getter
    @AllArgsConstructor
    public static class TriggerInput{
        String user;
        String message;
    }

}
