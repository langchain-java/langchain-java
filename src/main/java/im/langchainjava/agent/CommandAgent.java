package im.langchainjava.agent;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.command.CommandParser.Command;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommandAgent extends AsyncAgent{

    CommandParser commandParser;

    public CommandAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, CommandParser parser){
        super(prompt, memory, llm);
        this.commandParser = parser;
    }

    public abstract void onCommand(String user, Command command);

    @Override
    public boolean onUserMessage(String user, String message) {
        
        if(this.commandParser != null){
            Command command = this.commandParser.parse(message);
            if(command != null){
                log.info("Command:" + command.getCommand());
                onCommand(user, command);
                return false;
            }
        }
        return true;

    }
}
