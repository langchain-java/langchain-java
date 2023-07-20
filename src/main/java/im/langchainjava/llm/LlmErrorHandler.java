package im.langchainjava.llm;

public interface LlmErrorHandler {
    public void onAiException(String user, Exception e);

    public void onMaxTokenExceeded(String user);
}
