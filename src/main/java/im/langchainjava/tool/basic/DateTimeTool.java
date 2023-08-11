package im.langchainjava.tool.basic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.askuser.form.FormBuilders;
import im.langchainjava.utils.DateTimeUtils;
import im.langchainjava.utils.StringUtil;

public class DateTimeTool extends Tool{
    
    ImService im;

    LlmService llm;

    private static String PARAM_OFFSET = "offset_days";
    private static String PARAM_DESC = "The offset in days from today. Below listed meaning of some offset values: \"\"\"\n -1: Yesterday;\r\n 0: Today;\r\n 1: Tomorrow; \r\n 2: The day after tomorrow.\"\"\"";

    public DateTimeTool(ImService im, LlmService llm){
        this.im = im;
        this.llm = llm;
        dependencyAndProperty(im, FormBuilders.integerForm(llm, PARAM_OFFSET, PARAM_DESC));
    }

    @Override
    public String getName() {
        return "get_date_time";
    }

    @Override
    public String getDescription() {
        return  "This function provides date and time of given a day. You should always call this function whenever you need date time information.";
    }

    // @Override
    // public Map<String, FunctionProperty> getProperties() {
    //     Map<String, FunctionProperty> properties = new HashMap<>();
    //     FunctionProperty offset = FunctionProperty.builder()
    //             .description(PARAM_DESC)
    //             .build();
    //     properties.put(PARAM_OFFSET, offset);
    //     return properties;
    // }

    
    // @Override
    // public Map<String, ToolDependency> getDependencies() {
    //     Map<String, ToolDependency> deps = new HashMap<>();
    //     ToolDependency td = ToolDependency.builder()
    //             .dependency(new AskUserTool(im, FormBuilders.integerForm(llm, PARAM_OFFSET, PARAM_DESC)))
    //             .build();
    //     deps.put(PARAM_OFFSET, td);
    //     return deps;        
    // }

    // @Override
    // public List<String> getRequiredProperties() {
    //     List<String> required = new ArrayList<>();
    //     required.add(PARAM_OFFSET);
    //     return required;
    // }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        String offsetStr = ToolUtils.getStringParam(call, PARAM_OFFSET);
        int offset = 0;
        if(!StringUtil.isNullOrEmpty(offsetStr)){
            offset = Integer.valueOf(offsetStr);
        }
        Date offsetDate = DateTimeUtils.getOffsetedDate(offset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
        String message = "[日期时间]\n";
        if(offset == 0){
            message = message + "现在的UTC时间是：" + sdf.format(offsetDate) + "。";
        }else if(offset > 0){
            message = message + offset + "天后的UTC日期是：" + sdf2.format(offsetDate);
        }else{
            message = message + (-offset) + "天前的UTC日期是：" + sdf2.format(offsetDate);
        }
        im.sendMessageToUser(user, message);
        return ToolOuts.onResult(user, message);
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public List<String> getRequiredProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredProperties'");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }

}
