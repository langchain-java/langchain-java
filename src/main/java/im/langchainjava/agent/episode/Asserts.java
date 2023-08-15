package im.langchainjava.agent.episode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Asserts {
    public static void assertTrue(boolean condition, String message){
        if(!condition){
            log.error(message);
            throw new EpisodeException(message);
        }
    }
}
