package im.langchainjava.agent.controlledagent;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EpisodicPromptProvider implements ChatPromptProvider{

    @Getter
    final EpisodeSolver solver;

    public EpisodicPromptProvider(EpisodeSolver solver) {
        this.solver = solver;
    }

    public List<ChatMessage> getEpisodicControlPrompt(String user){
        Task task = this.solver.solveCurrentTask(user);

        Asserts.assertTrue(task != null, "Episode finished prematurely while calling getPrompt.");
        Asserts.assertTrue(!task.isFailed(), "Episode is failed while calling getEpisodicControlPrompt.");
        Asserts.assertTrue(task.getHistory() != null && !task.getHistory().isEmpty(), "There is no chat history in the task while calling getEpisodicControlPrompt.");

        StringBuilder promptBuilder = new StringBuilder()
                .append("Current UTC date time is : " + new SimpleDateFormat("yyyyMMdd-HH:mm:ss").format(new Date()) + "\r\n")
                .append("You are a conversation observer.\r\n")
                .append("Your will be provided with a conversation between the ai assistant and the user. The conversation includes function calls.\r\n")
                .append("Your task is to observe the conversation and extract the following information and use it as the function call input:\r\n")
                .append("\"\"\"");
        int i = 0;
        if(task.getExtractions() == null || task.getExtractions().isEmpty()){
            String msg = "The output extractions are not defined in task " + task.getName() + ".";
            log.error(msg);
            throw new EpisodeException(msg);
        }
        for(Entry<String, String> e: task.getExtractions().entrySet()){
            promptBuilder.append((++i) + ". " + e.getValue() + " And use it as the function input `" + e.getKey() + "`.\r\n");
        }
        String prompt = promptBuilder
                .append("\"\"\"")
                .append("Don't make assumptions about what values to plug into the generate_prompt function. You should leave the field blank if you don't know what value to put.\r\n")
                .toString();
        ChatMessage sysMsg = new ChatMessage(ROLE_SYSTEM, prompt);
        List<ChatMessage> chats = new ArrayList<>();
        chats.add(sysMsg);
        chats.addAll(task.getHistory());
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

    public List<ChatMessage> getEpisodicHistory(String user){
        Task task = this.solver.solveCurrentTask(user);
        Asserts.assertTrue(task != null, "Current Task is null while getting history.");
        Asserts.assertTrue(!task.isFailed(), "Episode is failed while getting history.");
        Asserts.assertTrue(task.getHistory() != null && !task.getHistory().isEmpty(), "There is no chat history in the task while getting history.");
        return task.getHistory();
    }

}
