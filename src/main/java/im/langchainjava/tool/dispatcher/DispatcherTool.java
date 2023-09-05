package im.langchainjava.tool.dispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.agent.episode.EpisodeSolver;
import im.langchainjava.agent.episode.model.Task;
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
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;

public class DispatcherTool extends Tool {

    final Map<String, Tool> tools;
    final ImService im;
    final EpisodeSolver solver;

    public DispatcherTool(EpisodeSolver solver, ImService im, Map<String, Tool> tools){
        super(true);
        this.im = im;
        this.tools = tools;
        this.solver = solver;
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

        // for (String t : this.tools.keySet()){
        //     List<String> enumm = new ArrayList<>();
        //     enumm.add("yes");
        //     enumm.add("no");
        //     FunctionProperty p = FunctionProperty.builder().description("does the user requires " + t + " (Must be one of {{yes, no}})? ")
        //             .enumerate(enumm)
        //             .build();
        //     properties.put("requires_" + t, p);
        // }
        List<String> enumm = new ArrayList<>();
        for(String t : this.tools.keySet()){
            enumm.add(t);
        }
        StringBuilder toolBuilder = new StringBuilder();
        for(String t : enumm){
            toolBuilder.append(t).append(",");
        }
        
        StringBuilder descBuilder = new StringBuilder("the next action to take (Must be one of {{")
                .append(toolBuilder.substring(0, toolBuilder.length() - 1))
                .append("}}).");

        FunctionProperty p = FunctionProperty.builder().description(descBuilder.toString())
                .enumerate(enumm)
                .build();
        properties.put("next_action", p); 

        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        // for (String t : this.tools.keySet()){
        //     required.add("next_action");
        // }
        required.add("next_action");
        return required;
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        return new HashMap<>();
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        Task task = this.solver.getCurrentTask(user);

        String action = ToolUtils.getStringParam(call, "next_action");
        if(!StringUtil.isNullOrEmpty(action)){
            if(this.tools.containsKey(action)){
                return ToolOuts.onDispatch(user, this.tools.get(action));
            }
        }

        // for(String t : this.tools.keySet()){
        //     if(ToolUtils.compareStringParamIgnoreCase(call, "requires_" + t, "yes")){
        //         if(task.getDispatched().contains(t)){
        //             continue;
        //         }
        //         task.dispatched(t);
        //         return ToolOuts.onDispatch(user, this.tools.get(t));
        //     }
        // }
        return ToolOuts.onDispatch(user, new SelfIntroductionTool(this.im));
    }
    
}
