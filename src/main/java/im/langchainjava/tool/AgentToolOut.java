package im.langchainjava.tool;

import lombok.Getter;
import lombok.Setter;

public class AgentToolOut implements ToolOut{

    @Getter
    final AgentToolOutStatus status;

    @Getter
    @Setter
    Tool successor;

    @Getter
    @Setter
    Tool dispatch;

    @Getter
    final ControlSignal control;

    @Getter
    final AgentToolError error;

    final String user;

    @Getter
    final String output;

    // public AgentToolOut(String user, AgentToolOutStatus status, String out){
    //     this.user = user;
    //     this.status = status;
    //     this.output = out;
    //     this.control = null;
    // }

    public AgentToolOut(String user, AgentToolOutStatus status, ControlSignal control, AgentToolError error, String out){
        this.user = user;
        this.status = status;
        this.output = out;
        this.control = control;
        this.error = error;
    }

    public static enum ControlSignal{
        ui,
        dispatch,
        form,
        finish;        
    }

    public static enum AgentToolOutStatus{
        control,
        success,
        error;
    }

    public static enum AgentToolError{
        invalidParam,
        empty,
        error;
    }

    public AgentToolOut successor(Tool tool){
        this.successor = tool;
        return this;
    }

    public AgentToolOut dispatch(Tool tool){
        this.dispatch = tool;
        return this;
    }

}
