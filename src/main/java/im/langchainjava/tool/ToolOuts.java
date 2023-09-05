package im.langchainjava.tool;

import im.langchainjava.tool.AgentToolOut.AgentToolError;
import im.langchainjava.tool.AgentToolOut.AgentToolOutStatus;
import im.langchainjava.tool.AgentToolOut.ControlSignal;
import im.langchainjava.tool.ControllorToolOut.Status;

public class ToolOuts{

    public static String KEY_FUNC_OUT = "function_out";
    public static String KEY_THOUGHT = "function_thought";
    public static String KEY_FUNC_OUT_ERR = "function_out_err";

    final String user;
    final Status status;
    final AgentToolOutStatus agentToolOutStatus;
    ControlSignal control;
    AgentToolError agentError;
    // String wrappedMessage;
    // final List<String> messageKeys;
    String controlOutput;
    String message;
    // String errorMessage;

    public static ToolOuts of(String user, AgentToolOutStatus status){
        return new ToolOuts(user, status);
    }

    public static ToolOuts of(String user, Status status, String output){
        return new ToolOuts(user, status, output);
    }
    
    public ToolOuts(String user, AgentToolOutStatus agentToolOutStatus){
        this.user = user;
        this.status = null;
        this.agentToolOutStatus = agentToolOutStatus;
        this.controlOutput = null;
        this.control = null;
        // this.messageKeys = new ArrayList<>();
    }

    public ToolOuts(String user, Status status, String output){
        this.user = user;
        this.status = status;
        this.control = null;
        this.agentToolOutStatus = null;
        this.controlOutput = output;
    }

    public ToolOuts message(String msg){
        this.message = msg;
        return this;
    }

    public ToolOuts agentControl(ControlSignal control){
        this.control = control;
        return this;
    }

    public ToolOuts agentError(AgentToolError error){
        this.agentError = error;
        return this;
    }
    
    public ToolOut get(){
        if(this.status == null){
            return new AgentToolOut(user, agentToolOutStatus, control, agentError, message);
        }else{
            return new ControllorToolOut(this.user, status, this.controlOutput, message);
        }
    }

    public static AgentToolOut onUi(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.control)
                        .message(message)
                        .agentControl(ControlSignal.ui)
                        .get();
    }

    public static AgentToolOut onAskUser(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.control)
                        .message(message)
                        .agentControl(ControlSignal.form)
                        .get();
    }

    public static AgentToolOut onResult(String user, String result){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.success)
                        .message(result)
                        .get();
    }

    public static AgentToolOut onResult(String user, String result, Tool successor){
        return ((AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.success)
                        .message(result)
                        .get()).successor(successor);
    }

    public static AgentToolOut onResult(String user, Tool successor){
        return ((AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.success)
                        .get()).successor(successor);
    }

    public static AgentToolOut onDispatch(String user, Tool dispatch){
        return ((AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.control)
                    .agentControl(ControlSignal.dispatch)
                    .get()).dispatch(dispatch);
    }

    public static AgentToolOut onFinish(String user, String result){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.control)
                        .message(result)
                        .agentControl(ControlSignal.finish)
                        .get();
    }

    public static AgentToolOut onToolError(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.error)
                        .agentError(AgentToolError.error)
                        .message(message)
                        .get();
    }

    public static AgentToolOut onEmptyResult(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.error)
                        .agentError(AgentToolError.empty)
                        .message(message)
                        .get();
    }

    public static AgentToolOut invalidParameter(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.error)
                        .agentError(AgentToolError.invalidParam)
                        .message(message)
                        .get();
    }

    public static ControllorToolOut halt(String user, String output, String error){
        return (ControllorToolOut) ToolOuts.of(user, Status.halt, output).message(error).get();
    }


    public static ControllorToolOut success(String user, String output){
        return  (ControllorToolOut) ToolOuts.of(user, Status.success, output).get();
    }

    public static ControllorToolOut failed(String user, String output, String error){
        return (ControllorToolOut) ToolOuts.of(user, Status.failed, output).message(error).get();
    }

    public static ControllorToolOut next(String user, String output){
        return (ControllorToolOut) ToolOuts.of(user, Status.next, output).get();
    }

    public static ControllorToolOut waitUserInput(String user, String output){
        return (ControllorToolOut) ToolOuts.of(user, Status.wait, output).get();
    }


}
