package im.langchainjava.tool.dispatcher;

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
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.introduce.SelfIntroductionTool;

public class DispatcherTool extends Tool {

    final Map<String, Tool> tools;
    final ImService im;

    public DispatcherTool(ImService im, Map<String, Tool> tools){
        this.im = im;
        this.tools = tools;
    }

    @Override
    public String getName() {
        return "dispatcher";
    }

    @Override
    public String getDescription() {
        return "dispatch the conversation to a tool";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        Map<String, FunctionProperty> properties = new HashMap<>();

        for (String t : this.tools.keySet()){
            FunctionProperty p = FunctionProperty.builder().description("does the user requires " + t + "? Must be one of {{yes, no}}").build();
            properties.put("requires_" + t, p);
        }

        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        return new ArrayList<>(this.tools.keySet());
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        return new HashMap<>();
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        for(String t : this.tools.keySet()){
            if(ToolUtils.compareStringParamIgnoreCase(call, "requires_" + t, "yes")){
                return ToolOuts.onDispatch(user, this.tools.get(t));
            }
        }
        return ToolOuts.onDispatch(user, new SelfIntroductionTool(this.im));
    }
    
}
