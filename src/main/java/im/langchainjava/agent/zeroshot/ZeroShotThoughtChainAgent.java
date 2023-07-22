package im.langchainjava.agent.zeroshot;

import java.util.List;

import im.langchainjava.agent.command.CommandParser;
import im.langchainjava.agent.command.CommandParser.Command;
import im.langchainjava.agent.controlledagent.ControlledAgent;
import im.langchainjava.agent.controlledagent.ControllerChatPromptProvider;
import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.ControllorToolOut;
import im.langchainjava.tool.Tool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeroShotThoughtChainAgent extends ControlledAgent{

    ImService wechatService;


    public ZeroShotThoughtChainAgent(LlmService llm, ControllerChatPromptProvider prompt, ChatMemoryProvider memory, ImService wechat, CommandParser cp, List<Tool> tools) {
        super(llm, prompt, memory, cp, tools);
        this.wechatService = wechat;
    }

    @Override
    public void onCommand(String user, Command command) {
        if(command.getCommand().equals("clear")){
            endConversation(user);
            return;
        }
        this.wechatService.sendMessageToUser(user, "[help]\n #clear: clears the chatbot memory.");
    }

    public void showMemory(String user){
        super.getMemoryProvider().showMemory(user);
    }

   
    @Override
    public void onMaxRound(String user) {
        wechatService.sendMessageToUser(user, "[系统]\n小助手已经达到最大的交互数。");
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
        endConversation(user);
    }

    @Override
    public void onCleardMemory(String user) {
        wechatService.sendMessageToUser(user, "[系统]\n记忆已经清除，让我们重新开始聊天吧。");
    }

    @Override
    public void onMessage(String user, String message) {
        wechatService.sendMessageToUser(user, message);
    }



}
