package im.langchainjava.tool;

import im.langchainjava.llm.entity.function.FunctionCall;

public class ToolUtils {
    
    public static String getStringParam(FunctionCall call, String paramName){
        if(call.getParsedArguments().containsKey(paramName)){
            if(call.getParsedArguments().get(paramName).isNull()){
                return null;
            }
            String retText = call.getParsedArguments().get(paramName).asText();
            return retText;
        }
        return null;
    }

    public static int getIntParam(FunctionCall call, String paramName){
        if(call.getParsedArguments().containsKey(paramName)){
            if(call.getParsedArguments().get(paramName).isNull()){
                return 0;
            }
            return call.getParsedArguments().get(paramName).asInt(0);
        }
        return 0;
    }

    public static boolean compareStringParam(FunctionCall call, String paramName, String value){
        if(value == null){
            return false;
        }
        if(call.getParsedArguments().containsKey(paramName)){
            String paramVal = call.getParsedArguments().get(paramName).asText();
            if(paramVal == null){
                return false;
            }
            return paramVal.equals(value);
        }
        return false;
    }

    public static boolean compareStringParamIgnoreCase(FunctionCall call, String paramName, String value){
        if(value == null){
            return false;
        }
        if(call.getParsedArguments().containsKey(paramName)){
            if(call.getParsedArguments().get(paramName).isNull()){
                return false;
            }
            String paramVal = call.getParsedArguments().get(paramName).asText();
            if(paramVal == null){
                return false;
            }
            return paramVal.equalsIgnoreCase(value);
        }
        return false;
    }
}
