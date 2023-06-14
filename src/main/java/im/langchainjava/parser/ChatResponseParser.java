package im.langchainjava.parser;

public interface ChatResponseParser<T> {
    Action<T> parse(String response) throws AiParseException;

    String getStructurePrompt();

    String getEnforceStructurePrompt();

    
    public static <T> boolean isCompleted(Action<T> action){
        return action != null && action instanceof Completion;
    }

    public static <T> boolean isFailure(Action<T> action){
        return action != null && action instanceof Failure;
    }

    public static <T> boolean isIntermedia(Action<T> action){
        return action == null || (!isCompleted(action) && !isFailure(action));
    }
}
