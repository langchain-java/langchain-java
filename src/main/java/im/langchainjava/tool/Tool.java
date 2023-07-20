package im.langchainjava.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.llm.entity.function.FunctionCall;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public interface Tool {

    
    public im.langchainjava.llm.entity.function.Function getFunction();
    public im.langchainjava.llm.entity.function.FunctionCall getFunctionCall();
    public ToolOut invoke(String user, FunctionCall action);





    @Getter
    @AllArgsConstructor
    public static class FunctionMessage{
        String user;
        String message;
    }

}
