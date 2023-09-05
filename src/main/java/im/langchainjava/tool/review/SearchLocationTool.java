package im.langchainjava.tool.review;

import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.review.ReviewService;
import im.langchainjava.review.ReviewService.CategoryEnum;
import im.langchainjava.review.model.Location;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.askuser.form.FormBuilders;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchLocationTool extends Tool{
    
    public static String PARAM_QUERY = "query";
    public static String PARAM_DESC_QUERY = "The query string for the location searching.";
    public static String PARAM_CATEGORY = "category";
    public static String PARAM_DESC_CATEGORY = "The location category. Must be one of {{hotels,attractions, restaurants}}. Using this property can make search result more accurate.";
    public static String PARAM_ADDR = "address";
    public static String PARAM_DESC_ADDR = "The address lines of the location that the user is searching for. Using this property can make search result more accurate.";
    public static String PARAM_PHONE = "phone";
    public static String PARAM_DESC_PHONE = "The phone number of the location that the user is searching. Using this property can make search result more accurate.";

    ImService im;

    ReviewService reviewService;

    LlmService llm;

    public SearchLocationTool(ImService im, ReviewService reviewService, LlmService llm){
        super(false);

        this.im = im;
        this.reviewService = reviewService;
        this.llm = llm;

        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_QUERY, PARAM_DESC_QUERY), true, false, true, false);
        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_CATEGORY, PARAM_DESC_CATEGORY), false, false, true, false);
        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_ADDR, PARAM_DESC_ADDR), false, false, true, false); 
        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_PHONE, PARAM_DESC_PHONE), false, false, true, false);
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
        String query = ToolUtils.getStringParam(call, PARAM_QUERY);
        String categoryStr = ToolUtils.getStringParam(call, PARAM_CATEGORY);
        String phone = ToolUtils.getStringParam(call, PARAM_DESC_PHONE);
        String address = ToolUtils.getStringParam(call, PARAM_ADDR);

        if(StringUtil.isNullOrEmpty(query)){
            return ToolOuts.invalidParameter(user, "The parameter query must not be null or empty.");
        }

        CategoryEnum category = null; 
        if(!StringUtil.isNullOrEmpty(categoryStr)){
            try{
                category = CategoryEnum.valueOf(categoryStr.trim().toLowerCase());
            }catch(IllegalArgumentException | NullPointerException e){
                log.info("The category value is invalid:" + categoryStr);
                category = null;
            }
        }

        if(StringUtil.isNullOrEmpty(phone)){
            phone = null;
        }

        if(StringUtil.isNullOrEmpty(address)){
            address = null;
        }

        try{

            List<Location> results = reviewService.searchLocations(query, category, address, phone);

            if(results == null || results.isEmpty()){
                String msg = "[推荐搜索]" + query + "\n" + "没有找到任何结果。";
                im.sendMessageToUser(user, msg); 
                return ToolOuts.onEmptyResult(user, msg);
            }

            im.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + "已经找到" + results.size()+"个结果。\n" + formatResults(results)); 
            return ToolOuts.onResult(user, JsonUtils.fromList(results));

        }catch(Exception e){
            e.printStackTrace();
            return ToolOuts.onToolError(user, "使用搜索引擎遇到故障：" + e.getMessage());
        }
    }

    private String formatResults(List<Location> outs){
        StringBuilder sb = new StringBuilder();
        for(Location o : outs){
            sb.append(o.getName()).append(":").append(o.getAddress().getCity()).append(o.getAddress().getAddress()).append("\n");
        }
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
