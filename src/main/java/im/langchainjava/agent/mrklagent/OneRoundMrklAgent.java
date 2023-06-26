package im.langchainjava.agent.mrklagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import im.langchainjava.agent.MemoryAgent;
import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.command.CommandParser.Command;
import im.langchainjava.agent.exception.FunctionCallException;
import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.prompt.ChatPromptProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.Tool.ToolOut;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneRoundMrklAgent extends MemoryAgent{

    ImService wechatService;

    Map<String, Tool> tools;

    public OneRoundMrklAgent(LlmService llm, ChatPromptProvider prompt, ChatMemoryProvider memory, ImService wechat, CommandParser cp, List<Tool> tools) {
        super(llm, prompt, memory, cp);
        this.wechatService = wechat;
        this.tools = new HashMap<>();
        for(Tool t: tools){
            this.tools.put(t.getFunction().getName(), t);
        }
    }

    @Override
    public void onCommand(String user, Command command) {
        if(command.getCommand().equals("clear")){
            clear(user);
            return;
        }
        this.wechatService.sendMessageToUser(user, "[help]\n #clear: clears the chatbot memory.");
    }

    public void showMemory(String user){
        super.getMemoryProvider().showMemory(user);
    }

    // observation
    final private Function<TriggerInput, Void> observation = input -> {
        super.getMemoryProvider().onReceiveFunctionCallResult(input.getUser(), "Function Call Result: \n" + input.getMessage() + " \n");
        return null; 
    };

    // think
    final private Function<TriggerInput, Void> thought = input -> {
        super.getMemoryProvider().onReceiveAssisMessage(input.getUser(), "Thought: " + input.getMessage() + " \n");
        return null;
    };
   
    private void clear(String user){
        super.getMemoryProvider().reset(user);
        for (Tool t : this.tools.values()){
            t.onClearedMemory(user);
        }
        wechatService.sendMessageToUser(user, "[系统]\n记忆已经清除，让我们重新开始聊天吧。");
    }

    @Override
    public void onMaxRound(String user) {
        wechatService.sendMessageToUser(user, "[系统]\n小助手已经达到最大的交互数。");
        clear(user);
    }

    @Override
    public void onUserMessageAtBusyTime(String user, String text) {
        this.wechatService.sendMessageToUser(user, "[系统]\n由于我正在尝试回答您的上一个问题，暂时只能忽略您的这个问题：" + text);
    }

    @Override
    public void onAiException(String user, Exception e) {
        this.wechatService.sendMessageToUser(user, "[系统]\n调用大模型的时候出错了，错误信息：\n" + e.getMessage() );
    }

    @Override
    public void onMaxTokenExceeded(String user) {
        wechatService.sendMessageToUser(user, "[系统]\n大模型记忆已经撑爆无法继续思考。");
        clear(user);
    }

    @Override
    public boolean onAssistantFunctionCall(String user, FunctionCall functionCall, String content) {

        if(functionCall == null){
            log.info("Function Call is null. This is unexpacted.");
            wechatService.sendMessageToUser(user, "Function Call is null. This is unexpacted.");
            return true;
        }

        if(this.tools.containsKey(functionCall.getName())){
            Tool t = this.tools.get(functionCall.getName());
            ToolOut toolOut = t.invoke(user, functionCall);
            if(toolOut == null){
                return onFunctionCallException(user, t, new FunctionCallException("Function call "+ t.getFunction().getName() + " returns null!"));
            }
            return toolOut.handlerForKey(Tool.KEY_OBSERVATION, observation)
                    .handlerForKey(Tool.KEY_THOUGHT, thought)
                    .apply(null);
        }

        return onFunctionCallException(user, null, new FunctionCallException("Function " + functionCall.getName() + " does not exist."));
    }

    @Override
    public boolean onAssistantMessage(String user, String content) {
        wechatService.sendMessageToUser(user, content);
        return true;
    }
}
