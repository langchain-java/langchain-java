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
        
        Asserts.assertTrue(this.task.getExtractions() != null && !this.task.getExtractions().isEmpty(), "Extractions are not set for task " + this.task.getName());

        Map<String, String> output = new HashMap<>();
        for(Entry<String, String> e : task.getExtractions().entrySet()){
            String key = e.getKey();
            if(key == null){
                continue;
            }
            String val = ToolUtils.getStringParam(call, key);

            if(StringUtil.isNullOrEmpty(val)){
                continue;
            }

            output.put(key, val);
        }

        if(output.size() < task.getExtractions().size()){
            // some progress is made, but not finished yet.
            return ToolOuts.next(user, output);
        }
        return ToolOuts.success(user, output);

    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // this method is only implemented for agent tools, this is a controller tool.
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }


}
