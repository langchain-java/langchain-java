package im.langchainjava.tool;

import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface ToolOut extends Runnable{
    public ToolOut handlerForKey(String key, Function<FunctionMessage, Void> fun);
    public String getMessageForKey(String key);

    @Getter
    @AllArgsConstructor
    public static class FunctionMessage{
        String user;
        String message;
    }

}