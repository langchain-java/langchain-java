package im.langchainjava.agent.mrklagent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import im.langchainjava.parser.Action;
import im.langchainjava.parser.AiParseException;
import im.langchainjava.parser.ChatResponseParser;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MrklChatResponseParser implements ChatResponseParser<String>{

    List<Tool> tools;

    Tool defaultTool = null;
    Pattern actionPattern;
    Pattern answerPattern;

    private static String ACTION = "action:";
    private static String ACTION_INPUT = "action Input:";
    private static String ACTION_INPUT2 = "input:";
    public static String FINAL_ANSWER = "final answer:";
    private static String INTENTION = "intention:";
    private static String THOUGHT = "thought:";
    private static String QUESTION = "question:";
    public MrklChatResponseParser(List<Tool> tools, Tool defaultTool){
        this.tools = tools; 
        this.defaultTool = defaultTool;
    }

    private String getValueOf(String response, String key){
        return getValueOf(response,key,false);
    }

    private String getValueOf(String response, String key, boolean multiLine){
        int start = response.indexOf(key);
        if(start == -1){
            return null;
        }
        if(!multiLine){
            return response.substring(start + key.length(), (response+"\n").indexOf("\n", start)).toLowerCase();
        }
        return response.substring(start + key.length()).toLowerCase();
    }

    @Override
    public Action<String> parse(String text) throws AiParseException {

        
        if(StringUtil.isNullOrEmpty(text)){
            return new Action<>("farewell", null, null, null, "[系统]\n脑袋里的东西太多，小助手已经无法思考。");
        }
        String response = text.trim().toLowerCase();
        
        String name = null;
        String input = null;
        String intention = getValueOf(response, INTENTION);
        input = getValueOf(response, FINAL_ANSWER, true);
        String question = getValueOf(response, QUESTION);
        String thought = getValueOf(response, THOUGHT);
        if(input != null){
            name = FINAL_ANSWER;
            return new Action<>(name, intention, thought, question, input);
        }

        name = getValueOf(response, ACTION);
        if(name == null){
            for(Tool t: tools){
                String tn = t.getToolName().trim().toLowerCase();
                if(response.indexOf(tn) >= 0){
                    name = tn;
                }
            }
        }
        String toolName = null;
        if(name != null){
            name = name.trim().toLowerCase();
            input = getValueOf(response, ACTION_INPUT, true);
            if(StringUtil.isNullOrEmpty(input)){
                input = getValueOf(response, ACTION_INPUT2, true); 
            }
            if(StringUtil.isNullOrEmpty(input)){
                input = getValueOf(response, name + ":", true);
            }
            if(StringUtil.isNullOrEmpty(input)){
                input = getValueOf(response, name, true);
            }
            for(Tool t:tools){
                if(name.indexOf(t.getToolName().toLowerCase()) != -1){
                    toolName = t.getToolName();
                    break;
                }
            }
            if(toolName == null && this.defaultTool != null){
                log.warn("Specified tool " + name +" is invalide. Use default tool:" + this.defaultTool.getToolName());
                toolName = defaultTool.getToolName();
            }
            return new Action<>(toolName, intention, thought, question, input);
        }

        log.warn("Can not parse action. Use ask_user tool");
        return new Action<>("ask_user", intention, thought, null, text);
    }

    @Override
    public String getStructurePrompt() {
        List<String> toolNames = new ArrayList<>();
        for(Tool t : this.tools){
            toolNames.add(t.getToolName());
        }
        String toolNameStr = String.join(",", toolNames);
        return "Use the following format:\n"
            +"Intention: the user intention you must fullfill. Sample: `Intention: user's intention.`\n"
            +"Thought: you should always think about what to do next would be best in helping fullfilling user's intention. Do not disclose the tool to use in the thought.\n" 
            +"Action: the action to take, should be one of ["
            +toolNameStr+"]. Example: `Action: the action name`. \n"
            +"Action Input: the input to the action. Always put the summary of the observation to the input if the input has useful information for the uesr's question. Example: `Action Input: the input`. \n"
            +"Observation: the result of the action\n"
            +"The user can not see your Intention/Thought/Action/Action Input/Observation.";
    }

    @Override
    public String getEnforceStructurePrompt() {
        return "Please response with the Thought/Action/Action Input/Observation loop again.";
    }
    
}
