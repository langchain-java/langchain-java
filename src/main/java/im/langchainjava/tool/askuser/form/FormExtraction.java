package im.langchainjava.tool.askuser.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import im.langchainjava.llm.entity.function.Function;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionParameter;
import im.langchainjava.llm.entity.function.FunctionProperty;

public class FormExtraction {

    public static String PARAMETER_TYPE_OBJECT = "object";

    public static String FUNC_NAME = "form_extraction";

    public static String FUNC_DESC = "extract function properties.";


    public static Function getFunction(Map<String, String> extractions){

        Map<String, FunctionProperty> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        for(Entry<String, String> e : extractions.entrySet()){
            required.add(e.getKey());
            FunctionProperty p = FunctionProperty.builder()
                .description(e.getValue())
                .build();
            properties.put(e.getKey(), p);
        }

        FunctionParameter parameter = FunctionParameter.builder()
                .type(PARAMETER_TYPE_OBJECT)
                .properties(properties)
                .required(required)
                .build();
        return new Function(FUNC_NAME, FUNC_DESC, parameter);
    }

    public static FunctionCall geFunctionCall(){
        return FunctionCall.builder().name(FUNC_NAME).build();
    }
}
