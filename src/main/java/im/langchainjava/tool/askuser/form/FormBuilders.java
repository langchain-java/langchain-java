package im.langchainjava.tool.askuser.form;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;

public class FormBuilders {

    public static String FORM_TYPE_TEXT = "text"; 
    public static String FORM_TYPE_NUMERIC = "numeric"; 
    public static String FORM_TYPE_SINGLE_CHOICE = "single_choice";
    public static String FORM_TYPE_MULTI_CHOICE = "multi_choice";
    public static String FORM_TYPE_DATE = "date";
    public static String FORM_TYPE_CITY = "city";
    public static String FORM_TYPE_SIMPLE = "simple_question";
    public static String FORM_TYPE_INTEGER = "integer";

    public static SmartFormBuilder textForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description, null)
                .type(FORM_TYPE_TEXT)
                .extraction("example_1", "Generate an answer to the question.")
                .extraction("example_2", "Generate a seconde answer to the question.")
                .extraction("example_3", "Generate a third answer to the question.");
    }

    public static SmartFormBuilder customizedForm(LlmService llm, String type, String name, String description, Map<String, String> properties){
        return Form.builder(llm, name, description, 
            new FormParamGenerator() {

                @Override
                public Map<String, String> getParameter(String user, FunctionCall call) {
                    return new HashMap<>(properties);
                }
                
            })
            .type(type);
    }

    public static SmartFormBuilder optionFrom(LlmService llm, String name, String description, boolean sigleChoice, List<String> options){
        SmartFormBuilder fb = Form.builder(llm, name, description, null);
        if(sigleChoice){
            fb.type(FORM_TYPE_SINGLE_CHOICE);
        }else{
            fb.type(FORM_TYPE_MULTI_CHOICE);
        }

        int i = 1;
        for(String opt : options){
            fb.extraction("option_" + (i++), "Translate this option into Chinese:`" + opt + "`");
        }

        return fb;
    }

    public static SmartFormBuilder dateForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description, 
            new FormParamGenerator() {

                @Override
                public Map<String, String> getParameter(String user, FunctionCall call) {
                    Map<String, String> params = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    params.put("today", sdf.format(new Date()));
                    return params;
                }
                
            })
                .type(FORM_TYPE_DATE);
    }

    public static SmartFormBuilder cityForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description, null)
                .type(FORM_TYPE_CITY);
    }

    public static SmartFormBuilder simpleQuestionForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description, null)
                .type(FORM_TYPE_SIMPLE);
    }

    public static SmartFormBuilder numericForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description, null)
                .type(FORM_TYPE_NUMERIC);
    }

    public static SmartFormBuilder integerForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description, null)
                .type(FORM_TYPE_INTEGER);
    }
}
