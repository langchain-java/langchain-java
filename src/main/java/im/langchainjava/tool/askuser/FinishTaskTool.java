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

public class FinishTaskTool extends Tool{

    private static String EXTRACTION_NAME = "status_of_task";
    private static String EXTRACTION = "the latest status of the task";
    // public static String PARAM_MSG = "message";

    // public static String PARAM_EXP_1 = "example1";
    // public static String PARAM_EXP_2 = "example2";
    // public static String PARAM_EXP_3 = "example3";


    ImService im;

    public FinishTaskTool(ImService im){
        super(false);
        this.im = im;
        extractionName(EXTRACTION_NAME);
        extraction(EXTRACTION);
    }

    @Override
    public String getName() {
        return "finish_task";
    }

    @Override
    public String getDescription() {
        return "Use this function to finish the current task. " ; 
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
        return ToolOuts.onFinish(user, "The task is finished.");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        return new HashMap<>();
    }

}