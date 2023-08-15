package im.langchainjava.agent.episode;

import static im.langchainjava.llm.LlmService.ROLE_SYSTEM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import im.langchainjava.agent.episode.model.Task;
import im.langchainjava.agent.episode.model.TaskExtraction;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.utils.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EpisodicPromptProvider implements ChatPromptProvider{

    private static String DEF_ROLE = "You are an ai assistant. ";
    private static String DEF_INSIGHT = "You may talk to the user or use a function to help you finish the task. You should finish the task by performing the following actions: \r\n"
            // +"\"\"\"\r\n" 
            +"1. Think what is the best action to finish the task.\r\n"
            +"2. If you have finished the task, you should end conversation. \r\n"
            +"3. You can ask the user to clarify his/her question or provide more information for the question.\r\n"
            +"4. You can make a function call from the function list to get information for user's requirement.\r\n"
            +"5. If you make a function call, don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous.\r\n"
            +"6. If you make a function call, don't leave function input values blank. You should ask the user to provide information of all the required parameters for a function call. \r\n"
            +"7. If there are already more than 3 function calls and still no good results to the user's requirement. You should try to answer the question with your own knowledge.\r\n"
            +"8. If you don't know the answer, it is hornest to tell the user you don't know the answer.\r\n"
            // +"\"\"\"\r\n"
            ;
    private static String DEF_STATEMENT = "Now let's work this out in a step by step way to be sure we have the right answer.\r\n";


    @Getter
    final EpisodeSolver solver;

    public EpisodicPromptProvider(EpisodeSolver solver) {
        this.solver = solver;
    }

    public List<ChatMessage> getEpisodicControlPrompt(String user){
        Task task = this.solver.solveCurrentTask(user);

        Asserts.assertTrue(task != null, "Episode finished prematurely while calling getPrompt.");
        Asserts.assertTrue(!task.isFailed(), "Episode is failed while calling getEpisodicControlPrompt.");
        // Asserts.assertTrue(task.getHistory() != null && !task.getHistory().isEmpty(), "There is no chat history in the task while calling getEpisodicControlPrompt.");

        StringBuilder promptBuilder = new StringBuilder()
                .append("The current UTC date is: " + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ". \r\n")
                .append("You are a conversation observer.\r\n")
                // .append("Your will be provided with a conversation between the ai assistant and the user. The conversation includes function calls.\r\n")
                .append("Your task is to observe the conversation and perform the following steps:\r\n")
                .append("\"\"\"\r\n");
        // if(task.getExtractions() == null || task.getExtractions().isEmpty()){
        if(task.getExtraction() == null){
            String msg = "The output extractions are not defined in task " + task.getName() + ".";
            log.error(msg);
            throw new EpisodeException(msg);
        }
        // int i = 0;
        // for(Entry<String, String> e: task.getExtractions().entrySet()){
        //     promptBuilder.append((++i) + ". " + e.getKey() + ": " + e.getValue() + ". And use it as the function input `" + e.getKey() + "`.\r\n");
        // }
        TaskExtraction e = task.getExtraction();
        promptBuilder.append( "1. Extract " + e.getDescription() + " from the chat history and use it as the function input `" + e.getName() + "`.\r\n");
        promptBuilder.append( "2. Decide whether the value of "+ e.getName() + " is a good match for its description: " + e.getDescription() + " And put yes or no to the function input `match`.\r\n");
        
        String prompt = promptBuilder
                .append("\"\"\"\r\n")
                .append("Always only extract values from the conversation. \r\n")
                .append("Don't make assumptions about what values to plug into the function. \r\n")
                .append("You should give the function input a blank value if you don't know what value to put.\r\n")
                .toString();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        List<ChatMessage> chats = new ArrayList<>();
        chats.add(sysMsg);
        chats.addAll(getEpisodicHistoryForTask(user, task));
        return chats;
    }

    public List<Function> getEpisodicControlFunctions(String user){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Current Task is null while calling getInvokableControllerFunction.");

        EpisodicControlTool controller = task.getEpisodicControlFunction();
        Asserts.assertTrue(controller != null, "EpisodeControlFunction is not set for task " + task.getName() + ".");

        return Collections.singletonList(controller.getFunction());
    }

    public FunctionCall getEpisodicControlFunctionCall(String user){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Current Task is null while calling getInvokableControllerFunction.");

        EpisodicControlTool controller = task.getEpisodicControlFunction();
        Asserts.assertTrue(controller != null, "EpisodeControlFunction is not set for task " + task.getName() + ".");

        return controller.getFunctionCall();
    }

    // public List<ChatMessage> getEpisodicHistory(String user){
    //     Task task = this.solver.solveCurrentTask(user);
    //     Asserts.assertTrue(task != null, "Current Task is null while getting history.");
    //     Asserts.assertTrue(!task.isFailed(), "Episode is failed while getting history.");
    //     // Asserts.assertTrue(task.getHistory() != null && !task.getHistory().isEmpty(), "There is no chat history in the task while getting history.");
    //     return getEpisodicHistoryForTask(user, task);
    // }

    public List<ChatMessage> getEpisodicHistoryForTask(String user, Task task){
        List<ChatMessage> history = new ArrayList<>();
        StringBuilder prefixBuilder = new StringBuilder();
        List<String> facts = new ArrayList<>();
        for(Entry<String, Task> e : task.getInputs().entrySet()){
            String extracted = e.getValue().getExtracted();
            if(StringUtil.isNullOrEmpty(extracted)){
                continue;
            }
            // for(Entry<String, String> tr : taskResult.entrySet()){
            //     facts.add("`" + tr.getKey() + "` = " + tr.getValue());
            // }
            facts.add("`" + e.getKey() + "` = " + extracted);
        }
        if(!facts.isEmpty()){
            prefixBuilder.append("Below are some values you already known:\r\n\"\"\"\r\n");
            for(String fact : facts){
                prefixBuilder.append(fact).append("\r\n");
            }
            prefixBuilder.append("\"\"\"\r\n");
            ChatMessage prefix = new ChatMessage(ROLE_SYSTEM, prefixBuilder.toString(), user, null);
            history.add(prefix);
        }
        if(task.getHistory() != null){
            history.addAll(task.getHistory());
        }
        return history;
    }

    
    @Override
    public List<ChatMessage> getPrompt(String user){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Episode finished prematurely while calling getPrompt.");
        Asserts.assertTrue(!task.isFailed(), "Episode is failed while calling getEpisodicControlPrompt.");

        List<ChatMessage> chats = new ArrayList<>();
        String dateStr = "The current UTC date is: " + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ". \r\n";
        // StringBuilder taskSb =  new StringBuilder("Your task is to find the following values: \r\n \"\"\"\r\n");
        // int i = 0;
        // for(Entry<String, String> e: task.getExtractions().entrySet()){
        //     taskSb.append((++i) + ". " + e.getKey() + ": " + e.getValue() + ". And use it as the function input `" + e.getKey() + "`.\r\n");
        // }
        TaskExtraction e = task.getExtraction();
        StringBuilder taskSb = new StringBuilder("Your task is to find out what is " + e.getDescription() + ".\r\n")
                .append("You can finish your task by either talking to the user or using a function call.\r\n");
        // taskSb.append("\"\"\"\r\n");

        StringBuilder promptSb = new StringBuilder(dateStr)
            .append(DEF_ROLE)
            .append(taskSb.toString())
            // .append(DEF_INSIGHT)
            .append(DEF_STATEMENT);
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, promptSb.toString());
        chats.add(sysMsg);
        chats.addAll(getEpisodicHistoryForTask(user, task));
        return chats;
    }


}
