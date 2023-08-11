package im.langchainjava.tool.askuser.form;

import java.util.List;

import im.langchainjava.llm.LlmService;

public class FormBuilders {

    public static String FORM_TYPE_TEXT = "text"; 
    public static String FORM_TYPE_NUMERIC = "numeric"; 
    public static String FORM_TYPE_SINGLE_CHOICE = "single_choice";
    public static String FORM_TYPE_MULTI_CHOICE = "multi_choice";
    public static String FORM_TYPE_DATE = "date";
    public static String FORM_TYPE_CITY = "city";
    public static String FORM_TYPE_SIMPLE = "simple_question";
    public static String FORM_TYPE_INTEGER = "integer";

    public static FormBuilder textForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description)
                .type(FORM_TYPE_TEXT)
                .extraction("example_1", "Generate an answer to the question.")
                .extraction("example_2", "Generate a seconde answer to the question.")
                .extraction("example_3", "Generate a third answer to the question.");
    }

    public static FormBuilder optionFrom(LlmService llm, String name, String description, boolean sigleChoice, List<String> options){
        FormBuilder fb = Form.builder(llm, name, description);
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

    public static FormBuilder dateForm(LlmService llm, String name, String description, String startDate){
        return Form.builder(llm, name, description)
                .type(FORM_TYPE_DATE)
                .property("start_date", startDate);
    }

    public static FormBuilder cityForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description)
                .type(FORM_TYPE_CITY);
    }

    public static FormBuilder simpleQuestionForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description)
                .type(FORM_TYPE_SIMPLE);
    }

    public static FormBuilder numericForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description)
                .type(FORM_TYPE_NUMERIC);
    }

    public static FormBuilder integerForm(LlmService llm, String name, String description){
        return Form.builder(llm, name, description)
                .type(FORM_TYPE_INTEGER);
    }
}
