package im.langchainjava.tool;

import java.util.function.Function;

import im.langchainjava.tool.Tool.FunctionMessage;

public interface ToolOut extends Runnable{
    public ToolOut handlerForKey(String key, Function<FunctionMessage, Void> fun);
}