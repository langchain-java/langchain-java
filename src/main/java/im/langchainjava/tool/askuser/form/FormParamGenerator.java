package im.langchainjava.tool.askuser.form;

import java.util.Map;

import im.langchainjava.llm.entity.function.FunctionCall;

public interface FormParamGenerator {
    Map<String, String> getParameter(String user, FunctionCall call);
}
