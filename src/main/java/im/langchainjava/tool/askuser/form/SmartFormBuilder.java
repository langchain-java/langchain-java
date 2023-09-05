package im.langchainjava.tool.askuser.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;
import lombok.Getter;

@Getter
public class SmartFormBuilder{
    public static String FORM_QUESTION = "_question";
    private static String USER = "agent";
    private static String FORM_GEN_PROMPT_ROLE      = "You are a text generator.\r\n";
    private static String FORM_GEN_PROMPT_INSTRUC   = "Your task is to generate the following texts accordingly:\r\n";

    final LlmService llm;
    final String name;
    final String description;
    final FormParamGenerator formParamGenerator;

    String type;
    
    Map<String, String> properties;
    Map<String, String> propertyExtraction;
    
    public SmartFormBuilder(LlmService llm, String name, String description, FormParamGenerator generator){
        this.llm = llm;
        this.name = name;
        this.description = description;
        this.formParamGenerator = generator; 
        properties = new HashMap<>();
        propertyExtraction = new HashMap<>();
        propertyExtraction.put(FORM_QUESTION, "Generate a fully formed question in Chinese to ask the user about the `" + name + "`. `" + name + "` is " + description + ".");
    }

    public SmartFormBuilder type(String type){
        this.type = type;
        return this;
    }

    public SmartFormBuilder property(String name, String value){
        this.properties.put(name, value);
        return this;
    }

    public SmartFormBuilder extraction(String name, String extraction){
        this.propertyExtraction.put(name, extraction);
        return this;
    }

    public SmartFormBuilder extractions(Map<String, String> extractions){
        if(extractions != null){
            this.propertyExtraction.putAll(extractions);
        }
        return this;
    }

    public Form build(String user, FunctionCall functionCall){

        String generatedQuestion = this.name;

        if(this.propertyExtraction != null && !this.propertyExtraction.isEmpty()){

            List<ChatMessage> prompt = new ArrayList<>();

            StringBuilder sysMsgBuilder = new StringBuilder()
                    .append(FORM_GEN_PROMPT_ROLE)
                    .append(FORM_GEN_PROMPT_INSTRUC)
                    .append("\"\"\"");
            for(Entry<String, String> e : this.propertyExtraction.entrySet()){
                sysMsgBuilder.append(e.getKey())
                    .append(":\t")
                    .append(e.getValue())
                    .append("\r\n");
            }
            sysMsgBuilder.append("\"\"\"");
            ChatMessage sys = new ChatMessage(LlmService.ROLE_SYSTEM, sysMsgBuilder.toString(), USER, null);
            prompt.add(sys);
            ChatMessage response = llm.chatCompletion(USER, prompt, Collections.singletonList(FormExtraction.getFunction(propertyExtraction)), FormExtraction.geFunctionCall(), null);
            
            if(response == null || response.getFunctionCall() == null){
                return null;
            }

            Map<String, JsonNode> param = Tool.parseFunctionCallParam(response.getFunctionCall());
            if(param == null){
                return null;
            }

            for(Entry<String, JsonNode> e : param.entrySet()){
                if(e.getValue() == null || e.getValue().isNull()){
                    continue;
                }
                if(e.getKey().equals(FORM_QUESTION)){
                    generatedQuestion = e.getValue().asText(generatedQuestion);
                    continue;
                }
                properties.put(e.getKey(), e.getValue().asText());
            }

            if(this.formParamGenerator != null){
                Map<String, String> generatedParam = this.formParamGenerator.getParameter(user, functionCall);
                if(generatedParam != null){
                    for(Entry<String, String> e : generatedParam.entrySet()){
                        if(StringUtil.isNullOrEmpty(e.getValue())){
                            continue;
                        }
                        properties.put(e.getKey(), e.getValue());
                    }
                }
            }
        }

        return new Form(type, generatedQuestion, properties);
    }
} 
