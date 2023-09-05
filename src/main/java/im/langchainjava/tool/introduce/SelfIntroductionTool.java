package im.langchainjava.tool.introduce;

import java.util.Collections;
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
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.askuser.form.FormBuilders;
import im.langchainjava.utils.StringUtil;

public class SelfIntroductionTool extends Tool{

    ImService im;

    public SelfIntroductionTool(ImService im){
        super(true);
        this.im = im;
    }

    @Override
    public String getName() {
        return "self_introduction";
    }

    @Override
    public String getDescription() {
        return "provide a self introduction";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        Map<String, FunctionProperty> properties = new HashMap<>();

        FunctionProperty p = FunctionProperty.builder().description("a short message for the user.").build();
        properties.put("message_to_user", p);

        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList("message_to_user");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        return new HashMap<>();
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        String message = ToolUtils.getStringParam(call, "message_to_user");
        // if(!StringUtil.isNullOrEmpty(message)){
            // im.sendMessageToUser(user, message);
        // }

        return ToolOuts.onAskUser(user, message);
    }
    
}
