package im.langchainjava.agent.controlledagent.model;

import lombok.Data;

@Data
public class TaskFailure {
    String message;
    Exception e;
}
