package im.langchainjava.agent.command;

import java.util.List;

import lombok.Data;

public interface CommandParser {
    Command parse(String message);
    
    @Data
    public static class Command{
        String command;
        List<String> parameters;
    }
}
