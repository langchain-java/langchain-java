package im.langchainjava.tool.review;

import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.review.ReviewService;
import im.langchainjava.review.model.LocationDetail;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationDetailTool extends Tool{
    
    public static String PARAM_ID = "location_id";
    public static String PARAM_DESC = "The location id extracted from search_locations tool.";

    ImService im;

    ReviewService reviewService;

    LlmService llm;

    public LocationDetailTool(ImService im, ReviewService reviewService, LlmService llm, SearchLocationTool searchLocationTool){
        this.im = im;
        this.reviewService = reviewService;
        this.llm = llm;

        dependencyAndProperty(PARAM_ID, PARAM_DESC, searchLocationTool, true);
    }

    @Override
    public String getName() {
        return "search_locations";
    }

    @Override
    public String getDescription() {
        return "This function is for searching recommendations of places to visit.\r\n"
                +"Always use this function when you need to search for attractions, hotels or restaurants.";
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        String id = ToolUtils.getStringParam(call, PARAM_ID);

        if(StringUtil.isNullOrEmpty(id)){
            return ToolOuts.invalidParameter(user, "The parameter location_id must not be null or empty.");
        }

        try{

            LocationDetail result = reviewService.getLocationDetails(id);

            if(result == null){
                String msg = "[地点详情]" + id + "\n" + "没有找到任何结果。";
                im.sendMessageToUser(user, msg); 
                return ToolOuts.onEmptyResult(user, msg);
            }

            im.sendMessageToUser(user, "[地点详情]" + "\n" + formatResults(result)); 
            return ToolOuts.onResult(user, JsonUtils.fromObject(result));

        }catch(Exception e){
            e.printStackTrace();
            return ToolOuts.onToolError(user, "使用搜索引擎遇到故障：" + e.getMessage());
        }
    }

    private String formatResults(LocationDetail location){
        StringBuilder sb = new StringBuilder();
        sb.append("名称:").append(location.getName()).append("\n")
                .append("名称:").append(location.getName()).append("\n")
                .append("描述:").append(location.getDescription()).append("\n")
                .append("链接:").append(location.getWebsite()).append("\n")
                .append("分数:").append(location.getRating()).append("\n");
        return sb.toString();
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        // this method will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public List<String> getRequiredProperties() {
        // this method will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredProperties'");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // this method will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }
}
