package im.langchainjava.tool.askuser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.askuser.form.Form;
import im.langchainjava.tool.askuser.form.SmartFormBuilder;
import im.langchainjava.utils.JsonUtils;

public class AskUserTool extends Tool{

    final ImService im;

    final SmartFormBuilder formBuilder;

    // final String tag;

    public AskUserTool(ImService im, SmartFormBuilder formBuilder){
        super(false);
        this.im = im;
        this.formBuilder = formBuilder;
        // this.tag = tag;
    }

    @Override
    public String getName() {
        return "ask_user";
    }

    @Override
    public String getDescription() {
        return "Use this tool to ask user a question." ; 
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        return new HashMap<>();
    }

    @Override
    public List<String> getRequiredProperties() {
        return new ArrayList<>();
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall functionCall, ChatMemoryProvider memory) {
        
        if(this.formBuilder == null){
            return ToolOuts.onToolError(user, "The ask user tool does not has a form builder.");
        }

        Form form = this.formBuilder.build(user, functionCall);
        // this.im.sendMessageToUser(user, form.getMessage());
        // this.im.sendMessageToUser(user, JsonUtils.fromObject(form));
        return ToolOuts.onAskUser(user, JsonUtils.fromObject(form));
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        return new HashMap<>();
    }

}