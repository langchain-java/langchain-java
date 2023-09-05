package im.langchainjava.tool.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.location.LocationService;
import im.langchainjava.location.LocationService.Location;
import im.langchainjava.location.LocationService.Place;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.askuser.form.FormBuilders;
import im.langchainjava.utils.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleLocationTool extends Tool{

    public static int NUM_RESULT = 5;

    public static String PARAM_PLACE = "place";
    public static String PARAM_DESC_PLACE = "The place name (in Chinese) to look up its address.";
    public static String PARAM_CITY = "city";
    public static String PARAM_DESC_CITY = "The city name (in Chinese) of the place in the query.";

    ImService im;

    LocationService locationService;

    LlmService llm;

    int number;

    public SimpleLocationTool(ImService im, LocationService location, LlmService llm){
        super(false);

        this.im = im;
        this.locationService = location;
        this.llm = llm;
        this.number = NUM_RESULT;

        dependencyAndProperty(im, FormBuilders.cityForm(llm, PARAM_CITY, PARAM_DESC_CITY), true, false, true, false);
        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_PLACE, PARAM_DESC_PLACE), true, false, true, false);
    }

    public SimpleLocationTool numberOfResults(int num){
        this.number = num;
        return this;
    }

    @Override
    public String getName() {
        return "lookup_address_by_place";
    }

    @Override
    public String getDescription() {
        return  "This function returns the address lines and map location of a explicitly given place.\r\n"
                +"Only use this function when use's question is looking up the address of a place.\r\n"
                +"Don't use this function to search recommendations of places or attractions.\r\n"
                +"The output of this function could not be recommended to the user as places to visit..";
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        try{
            // String place = call.getParsedArguments().get(PARAM_PLACE).asText();
            // String city = call.getParsedArguments().get(PARAM_CITY).asText();
            String place = ToolUtils.getStringParam(call, PARAM_PLACE);
            String city = ToolUtils.getStringParam(call, PARAM_CITY);

            if(StringUtil.isNullOrEmpty(city)){
                return ToolOuts.invalidParameter(user, "function input " + PARAM_CITY + " can not be empty.");
            }
            if(StringUtil.isNullOrEmpty(place)){
                return ToolOuts.invalidParameter(user, "function input " + PARAM_PLACE + " can not be empty.");
            }
            List<Place> places = locationService.queryPlaceWithDetail(place, city); 
            if(places == null || places.isEmpty()){
                String emptyMsg = "[地图]"+city + "," + place+"\n" + "查找不到任何结果";
                im.sendMessageToUser(user, emptyMsg);
                return ToolOuts.onEmptyResult(user, emptyMsg);
            }
            List<LocationOutput> locations = new ArrayList<>();
            int n = 0;
            for(Place p : places){
                if(n++ >= this.number){
                    break;
                }

                LocationOutput output = new LocationOutput();
                output.setName(p.getName());
                output.setAddress(p.getAddress());
                output.setCity(p.getCity());
                output.setProvince(p.getProvince());
                output.setLocation(p.getLocation());
                output.setLink(p.getUrl());
                locations.add(output);
            }

            String result = formatResults(locations);
            this.im.sendMessageToUser(user, result);
            return ToolOuts.onResult(user, result);
            // ChatMessage response = null;
            // LocationLlmErrorHandler h = new LocationLlmErrorHandler(locations, false);
            // String query = "查找" + city + place+ "的地址";
            // while(true){
            //     response = this.llm.chatCompletion(user, getPrompt(query, h.getOuts()), null, null, h);
            //     if(response != null || !h.isShouldRetry()){
            //         break;
            //     }
            // }
            // if(response != null && response.getContent() != null){
            //     wechat.sendMessageToUser(user, "[地图]"+city + "," + place+"\n" + response.getContent());
            //     return ToolOuts.onResult(user, response.getContent());
            // }
            // return ToolOuts.onToolError(user,  );
        }catch(Exception e){
            return ToolOuts.onToolError(user, "调用地图工具发生了错误。" + e.getMessage());
        }
    }

    private String formatResults(List<LocationOutput> outs){
        StringBuilder sb = new StringBuilder();
        for(LocationOutput o : outs){
            sb.append(o.getName()).append("|").append(o.getAddress()).append("|").append(o.getLink()).append("\n");
        }
        return sb.toString();
    }



    // private List<ChatMessage> getPrompt(String query, List<LocationOutput> outs){
    //         String resp = JsonUtils.fromList(outs);
           
    //         String prompt = new StringBuilder()
    //             .append("You are an function output reviewer. Your task is to run the following steps with the provided function outputs:\r\n")
    //             .append("1. Select at most 3 useful results from the function outputs to answer the function query.\r\n")
    //             .append("2. Summarize the selected results with a short scentence.\r\n")
    //             .append("Function Query:\"\"\"\r\n")
    //             .append(query)
    //             .append("\"\"\"\r\n")
    //             .append("Function Output:\"\"\"\r\n")
    //             .append(resp)
    //             .append("\"\"\"\r\n")
    //             .append("Your response should be in the following format:\"\"\"\r\n")
    //             .append("<one-scentence summary of the selected results>\r\n")
    //             .append("1. <summary of selected result 1 in Chinese>:<url of selected result 1>\r\n")
    //             .append("2. <summary of selected result 2 in Chinese>:<url of selected result 2>\r\n")
    //             .append("3. <summary of selected result 3 in Chinese>:<url of selected result 3>\"\"\"\r\n")
    //             .toString();
    //         log.info(prompt);
    //         ChatMessage msg = new ChatMessage(ROLE_SYSTEM, prompt);
    //         List<ChatMessage> msgs = new ArrayList<>();
    //         msgs.add(msg);
    //         return msgs;
    // }

    // @Data
    // @AllArgsConstructor
    // public static class LocationLlmErrorHandler implements LlmErrorHandler {
    //     List<LocationOutput> outs;
    //     boolean shouldRetry = false;
    //     @Override
    //     public void onAiException(String user, Exception e) {
    //         shouldRetry = false;
    //     }

    //     @Override
    //     public void onMaxTokenExceeded(String user) {
    //         this.shouldRetry = false;
    //         if(outs.size() >= 4){
    //             log.info("Shrinking search engine output list.");
    //             shouldRetry = true;
    //             outs = outs.subList(0, outs.size()/2);
    //         }
    //     }
    // }

    @Data
    @NoArgsConstructor
    public static class LocationOutput{
        String name;
        Location location;
        String province;
        String city;
        String address;
        String link;
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        // will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public List<String> getRequiredProperties() {
        // will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredProperties'");
    }

}
