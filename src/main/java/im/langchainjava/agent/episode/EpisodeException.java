package im.langchainjava.agent.episode;

public class EpisodeException extends RuntimeException{
    public EpisodeException(String message, Exception cause){
        super(message, cause);
    }
    public EpisodeException(String message){
        super(message);
    }
}
