package im.langchainjava.agent.functioncall;

import im.langchainjava.agent.episode.model.Task;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.tool.Tool;

public interface TaskSolver {
    Task solveFunctionCall(String user, FunctionCall call, Tool given);
}
