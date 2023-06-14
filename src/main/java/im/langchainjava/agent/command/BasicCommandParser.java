package im.langchainjava.agent.command;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class BasicCommandParser implements CommandParser{

    @Override
    public Command parse(String message) {
        if(message.startsWith("#")){
            String cmdStr = message.substring(1);
            String[] cmdArr = cmdStr.split(" ");
            int cmdLength = Array.getLength(cmdArr);
            if(cmdLength == 0){
                return null;
            }
            Command cmd = new Command();
            cmd.setCommand(cmdArr[0]);
            List<String> parm = new ArrayList<>();
            for(int i = 1; i< cmdLength; i++){
                parm.add(cmdArr[i]);
            }
            cmd.setParameters(parm);
            return cmd;
        }
        return null;
    }
    
}
