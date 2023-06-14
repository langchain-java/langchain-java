package im.langchainjava.agent.mrklagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.agent.MemoryAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.command.CommandParser.Command;
import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.parser.ChatResponseParser;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.Tool.ToolOut;
import im.langchainjava.utils.StringUtil;

public class OneRoundMrklAgent extends MemoryAgent{

    ImService wechatService;

    Map<String, Tool> tools;

    public OneRoundMrklAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, ImService wechat, ChatResponseParser<?> parser, CommandParser cp, List<Tool> tools) {
        super(llm, prompt, memory, parser,cp);
        this.wechatService = wechat;
        this.tools = new HashMap<>();
        for(Tool t: tools){
            this.tools.put(t.getToolName(), t);
        }
    }

    @Override
    public void onCommand(String user, Command command) {
        if(command.getCommand().equals("clear")){
            clear(user);
            return;
        }
        this.wechatService.sendMessageToUser(user, "[help]\n #clear to clear the chatbot memory.");
    }

    public void showMemory(String user){
        super.getMemoryProvider().showMemory(user);
    }

    @Override
    public boolean onAssistantResponse(String user, Action<?> action) {
        if(action == null){
            wechatService.sendMessageToUser(user, "Action is null. This is unexpacted.");
            return true;
        }

        if(!StringUtil.isNullOrEmpty(action.getThought())){
            this.wechatService.sendMessageToUser(user, "[Thought]\n" + action.getThought());
        }

        if(action.getName().equals(MrklChatResponseParser.FINAL_ANSWER)){
            String msg = String.valueOf(action.getInput());
            wechatService.sendMessageToUser(user, msg);
            return true;
        }

        if(this.tools.containsKey(action.getName())){
            Tool t = this.tools.get(action.getName());
            ToolOut toolOut = t.invoke(user, action);
            if(toolOut == null){
                onInternalError(user, action.getName(), "The tool returns null!");
            }
            return toolOut.handlerForKey(Tool.KEY_OBSERVATION, observation)
                    .handlerForKey(Tool.KEY_THOUGHT, thought)
                    .apply(null);
        }
        return true;
    }

    // observation
    final private Function<TriggerInput, Void> observation = input -> {
        super.getMemoryProvider().onReceiveAssisMessage(input.getUser(), "Observation: " + input.getMessage() + " \n");
        return null;
    };

    // observation and think
    final private Function<TriggerInput, Void> thought = input -> {
        super.getMemoryProvider().onReceiveAssisMessage(input.getUser(), "Thought: " + input.getMessage() + " \n");
        return null;
    };


    @Override
    public boolean onUnexpactedAssistantResponse(String user, String raw) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onUnexpactedAssistantResponse'");
    }

    @Override
    public void onInternalError(String user, String raw, String errorMessage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onInternalError'");
    }
    
    private void clear(String user){
        super.getMemoryProvider().reset(user);
        for (Tool t : this.tools.values()){
            t.onClearedMemory(user);
        }
        wechatService.sendMessageToUser(user, "[系统]\n记忆已经清除，让我们重新开始聊天吧。");
    }

    @Override
    public void onMaxRound(String user) {
        wechatService.sendMessageToUser(user, "[系统]\n小助手的记忆已经撑爆无法继续思考，请回复#clear重新开始交互吧。");
    }

    @Override
    public void onUserMessageAtBusyTime(String user, String text) {
        this.wechatService.sendMessageToUser(user, "[系统]\n由于我正在尝试回答您的上一个问题，暂时只能忽略您的这个问题：" + text);
    }

    @Override
    public void onAiException(String user, Exception e) {
        this.wechatService.sendMessageToUser(user, "[系统]\n因为和大模型的连接超时了，我暂时无法回答您这个问题，请再尝试问我问题。" );
    }
}
