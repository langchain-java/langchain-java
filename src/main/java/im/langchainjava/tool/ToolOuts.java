package im.langchainjava.tool;

import java.util.HashMap;
import java.util.Map;

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
    String wrappedMessage;
    // final List<String> messageKeys;
    final Map<String, String> controlOutput;
    String message;
    // String errorMessage;

    public static ToolOuts of(String user, AgentToolOutStatus status){
        return new ToolOuts(user, status);
    }

    public static ToolOuts of(String user, Status status, Map<String, String> output){
        return new ToolOuts(user, status, output);
    }
    
    public ToolOuts(String user, AgentToolOutStatus agentToolOutStatus){
        this.user = user;
        this.status = null;
        this.agentToolOutStatus = agentToolOutStatus;
        this.controlOutput = new HashMap<>();
        this.control = null;
        // this.messageKeys = new ArrayList<>();
    }

    public ToolOuts(String user, Status status, Map<String, String> output){
        this.user = user;
        this.status = status;
        this.control = null;
        this.agentToolOutStatus = null;
        if(output == null){
            this.controlOutput = new HashMap<>();
        }else{
            this.controlOutput = new HashMap<>(output);
        }
    }

    // public ToolOuts message(String key, String message){
    //     if(this.messages.containsKey(key)){
    //         throw new ToolException("Duplcated message key: " + key);
    //     }
    //     // this.messageKeys.add(key);
    //     this.messages.put(key, message);
    //     return this;
    // } 
    public ToolOuts message(String msg){
        this.message = msg;
        return this;
    }

    public ToolOuts output(String key, String message){
        this.controlOutput.put(key, message);
        return this;
    }

    public ToolOuts agentControl(ControlSignal control){
        this.control = control;
        return this;
    }

    // public ToolOuts errorMessage(String msg){
    //     this.errorMessage = msg;
    //     return this;
    // }

    public ToolOuts wrapAssistantMessage(String message){
        this.wrappedMessage = message;
        return this;
    }

    
    public ToolOut get(){
        if(this.status == null){
            return new AgentToolOut(user, agentToolOutStatus, message);
        }else{
            return new ControllorToolOut(this.user, status, this.controlOutput, message);
        }
    }


    public static AgentToolOut invalidParameter(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.invalideParam)
                        .message(message)
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

    public static AgentToolOut onFinish(String user, String result){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.control)
                        .message(result)
                        .agentControl(ControlSignal.finish)
                        .get();
    }

    public static AgentToolOut onToolError(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.error)
                        .message(message)
                        .get();
    }

    public static AgentToolOut onEmptyResult(String user, String message){
        return (AgentToolOut) ToolOuts.of(user, AgentToolOutStatus.empty)
                        .message(message)
                        .get();
    }

    public static ControllorToolOut halt(String user, Map<String, String> output, String error){
        return (ControllorToolOut) ToolOuts.of(user, Status.halt, output).message(error).get();
    }


    public static ControllorToolOut success(String user, Map<String, String> output){
        return  (ControllorToolOut) ToolOuts.of(user, Status.success, output).get();
    }

    public static ControllorToolOut failed(String user, Map<String, String> output, String error){
        return (ControllorToolOut) ToolOuts.of(user, Status.failed, output).message(error).get();
    }

    public static ControllorToolOut next(String user, Map<String, String> output){
        return (ControllorToolOut) ToolOuts.of(user, Status.next, output).get();
    }

    public static ControllorToolOut waitUserInput(String user, Map<String, String> output){
        return (ControllorToolOut) ToolOuts.of(user, Status.wait, output).get();
    }


}
