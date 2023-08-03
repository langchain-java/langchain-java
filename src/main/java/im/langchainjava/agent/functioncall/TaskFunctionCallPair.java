package im.langchainjava.agent.functioncall;

import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.llm.entity.function.FunctionCall;
import lombok.Data;

@Data
public class TaskFunctionCallPair {
    Task task; 
    FunctionCall call; 
}
