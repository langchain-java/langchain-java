package im.langchainjava.agent.controlledagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EpisodicControlTool extends Tool{

    final Task task;
    final Map<String, FunctionProperty> properties;
    final List<String> required;

    public EpisodicControlTool(Task task) {
        this.task = task;
        this.properties = new HashMap<>();
        if(task.getExtractions() == null){
            String msg = "Result extractions are null for task " + task.getName();
            log.error(msg);
            throw new EpisodeException(msg);
        }
        for(Entry<String, String> extraction : task.getExtractions().entrySet()){
            this.properties.put(extraction.getKey(), FunctionProperty.builder().description(extraction.getValue()).build());
        }
        this.required = new ArrayList<>();
        this.required.addAll(this.properties.keySet());
    }

    @Override
    public String getName() {
        return "generate_prompt";
    }

    @Override
    public String getDescription() {
        return "This function is used to generate prompt for an ai assistant.";
    }


    @Override
    public Map<String, FunctionProperty> getProperties() {
        return this.properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        return this.required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        
        if(this.task.getExtractions() == null || this.task.getExtractions().isEmpty()){
            String msg = "Extractions are not set for task " + this.task.getName();
            log.error(msg);
            throw new EpisodeException(msg);
        }

        for(Entry<String, String> e : this.task.getExtractions().entrySet()){
            String paramName = e.getKey();
    
            String paramValue = ToolUtils.getStringParam(call, paramName);
    
            if(StringUtil.isNullOrEmpty(paramValue)){
                return ToolOuts.failed(user, null, null);
            }
        }
        return ToolOuts.success(user, null, null);

    }

    @Override
    public String getTag() {
        // this method is only implemented for agent tools, this is a controller tool.
        throw new UnsupportedOperationException("Unimplemented method 'getTag'");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // this method is only implemented for agent tools, this is a controller tool.
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }


}
