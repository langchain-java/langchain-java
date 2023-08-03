package im.langchainjava.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.tool.ControllorToolOut.Status;
import im.langchainjava.utils.StringUtil;

import static im.langchainjava.tool.Tool.KEY_FUNC_OUT;
import static im.langchainjava.tool.Tool.KEY_THOUGHT;
import static im.langchainjava.tool.Tool.KEY_CONTROL_SUMMARY;
import static im.langchainjava.tool.Tool.KEY_CONTROL_ASK;

public class ToolOuts{
    final String user;
    final Status status;
    final List<String> messageKeys;
    final Map<String, String> messages;

    public static ToolOuts of(String user){
        return new ToolOuts(user);
    }

    public static ToolOuts of(String user, Status status){
        return new ToolOuts(user, status);
    }
    
    public ToolOuts(String user){
        this.user = user;
        this.status = null;
        this.messages = new HashMap<>();
        this.messageKeys = new ArrayList<>();
    }

    public ToolOuts(String user, Status status){
        this.user = user;
        this.status = status;
        this.messages = new HashMap<>();
        this.messageKeys = new ArrayList<>();
    }

    public ToolOuts message(String key, String message){
        if(this.messages.containsKey(key)){
            throw new ToolException("Duplcated message key: " + key);
        }
        this.messageKeys.add(key);
        this.messages.put(key, message);
        return this;
    } 

    
    public ToolOut get(){
        List<ToolOutHandler> handlers = new ArrayList<>();
        Map<String, ToolOutHandler> handlerMap = new HashMap<>();
        for(String key : this.messageKeys){
            ToolOutHandler handler = new ToolOutHandler();
            String message = this.messages.get(key);
            handler.setKey(key);
            handler.setMessage(message);
            handlers.add(handler);
            handlerMap.put(key, handler);
        }
        if(this.status == null){
            return new AgentToolOut(this.user, handlers, handlerMap);
        }else{
            return new ControllorToolOut(user, handlers, handlerMap, status);
        }
    }


    public static ToolOut invalidParameter(Tool tool, String user, String message){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, tool.getObservationOnInvalidParameter(message))
                        .message(KEY_THOUGHT, tool.getThoughtOnInvalidParameter())
                        .get();
    }

    // public ToolOut waitUserInput(String user){
    //     ToolOut out = ToolOuts.of(user, false)
    //                         .message(KEY_FUNC_OUT, "")
    //                         .sync();
    //     // this.users.put(user, out);
    //     return out;
    // }

    public static ToolOut onResult(Tool tool, String user, String result){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, result)
                        .message(KEY_THOUGHT, tool.getThought())
                        .get();
    }

    // public ToolOut onDisclosedResult(String user, String result, String disclosedResult){
    //     return ToolOuts.of(user, true)
    //                     .message(Tool.KEY_FUNC_OUT, result)
    //                     .message(Tool.KEY_THOUGHT, getThought())
    //                     .message(Tool.KEY_DISCLOSE, disclosedResult)
    //                     .sync();
    // }

    public static ToolOut onToolError(Tool tool, String user){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, tool.getObservationOnError())
                        .message(KEY_THOUGHT, tool.getThoughtOnError())
                        .get();
    }

    public static ToolOut onEmptyResult(Tool tool, String user){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, tool.getObservationOnEmptyResult())
                        .message(KEY_THOUGHT, tool.getThoughtOnEmptyResult())
                        .get();
    }

    public static ToolOut onEmptyResult(Tool tool, String user, String message){
        return ToolOuts.of(user)
                        .message(KEY_FUNC_OUT, message)
                        .message(KEY_THOUGHT, tool.getThoughtOnEmptyResult())
                        .get();
    }


    public static ControllorToolOut halt(String user, String message, String ask){
        return (ControllorToolOut) ToolOuts.of(user, Status.halt)
                        .message(KEY_CONTROL_SUMMARY, message)
                        .message(KEY_CONTROL_ASK, ask)
                        .get();
    }


    public static ControllorToolOut success(String user, String message, String ask){
        return (ControllorToolOut) ToolOuts.of(user, Status.success)
                        .message(KEY_CONTROL_SUMMARY, message)
                        .message(KEY_CONTROL_ASK, ask)
                        .get();
    }

    public static ControllorToolOut failed(String user, String message, String ask){
        return (ControllorToolOut) ToolOuts.of(user, Status.failed)
                        .message(KEY_CONTROL_SUMMARY, message)
                        .message(KEY_CONTROL_ASK, ask)
                        .get();
    }

    public static ToolOut next(String user, String thought, String message, String ask){
        ToolOuts to = ToolOuts.of(user, Status.next);
        if(!StringUtil.isNullOrEmpty(thought)){
            to.message(KEY_THOUGHT, thought);
        }
        if(!StringUtil.isNullOrEmpty(message)){
            to.message(KEY_CONTROL_SUMMARY, message);
        }
        if(!StringUtil.isNullOrEmpty(ask)){
            to.message(KEY_CONTROL_ASK, ask);
        }
        return to.get();
    }

    public static ToolOut waitUserInput(String user, String thought, String message, String ask){
        ToolOuts to = ToolOuts.of(user, Status.wait);
        if(!StringUtil.isNullOrEmpty(thought)){
            to.message(KEY_THOUGHT, thought);
        }
        if(!StringUtil.isNullOrEmpty(message)){
            to.message(KEY_CONTROL_SUMMARY, message);
        }
        if(!StringUtil.isNullOrEmpty(ask)){
            to.message(KEY_CONTROL_ASK, ask);
        }
        return to.get();
    }


}
