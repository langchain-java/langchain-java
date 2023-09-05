package im.langchainjava.tool.askuser.form;

import java.util.HashMap;
import java.util.Map;

import im.langchainjava.llm.LlmService;
import lombok.Getter;

@Getter
public class Form {

    final String type;
    
    final String message;
    
    final Map<String, String> properties;

    public Form(String type, String message, Map<String, String> properties){
        this.type = type;
        this.message = message;
        this.properties = new HashMap<>();
        if(properties != null){
            this.properties.putAll(properties);
        }
    }

    public static SmartFormBuilder builder(LlmService llm, String name, String description, FormParamGenerator generator){
        return new SmartFormBuilder(llm, name, description, generator); 
    }

}
