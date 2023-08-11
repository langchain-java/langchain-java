package im.langchainjava.agent.functioncall;

import im.langchainjava.agent.controlledagent.model.Task;
import im.langchainjava.llm.entity.function.FunctionCall;

public interface TaskSolver {
    Task solveFunctionCall(String user, FunctionCall call);
}
