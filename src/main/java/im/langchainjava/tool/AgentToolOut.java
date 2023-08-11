package im.langchainjava.tool;

import lombok.Getter;

public class AgentToolOut implements ToolOut{

    @Getter
    final AgentToolOutStatus status;

    @Getter
    final ControlSignal control;

    final String user;

    @Getter
    final String output;

    public AgentToolOut(String user, AgentToolOutStatus status, String out){
        this.user = user;
        this.status = status;
        this.output = out;
        this.control = null;
    }

    public AgentToolOut(String user, AgentToolOutStatus status, ControlSignal control, String out){
        this.user = user;
        this.status = status;
        this.output = out;
        this.control = control;
    }

    public static enum ControlSignal{
        form,
        finish;        
    }

    public static enum AgentToolOutStatus{
        control,
        invalideParam,
        success,
        empty,
        error;
    }

}
