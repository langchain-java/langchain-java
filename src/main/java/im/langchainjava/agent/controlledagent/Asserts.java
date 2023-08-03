package im.langchainjava.agent.controlledagent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Asserts {
    public static void assertTrue(boolean condition, String message){
        if(!condition){
            String msg = "Current Task is null while calling getPrompt.";
            log.error(msg);
            throw new EpisodeException(msg);
        }
    }
}
