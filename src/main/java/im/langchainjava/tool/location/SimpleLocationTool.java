package im.langchainjava.tool.location;

import java.util.ArrayList;
import java.util.HashMap;
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
import im.langchainjava.tool.BasicTool;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SimpleLocationTool extends BasicTool{

    public static int NUM_RESULT = 5;

    public static String PARAM_PLACE = "place";
    public static String PARAM_CITY = "city";

    ImService wechat;

    LocationService locationService;

    LlmService llm;

    int number;

    public SimpleLocationTool(ChatMemoryProvider memory, ImService wechat, LocationService location, LlmService llm){
        super(memory);
        this.wechat = wechat;
        this.locationService = location;
        this.llm = llm;
        this.number = NUM_RESULT;
    }

    public SimpleLocationTool numberOfResults(int num){
        this.number = num;
        return this;
    }

    @Override
    public String getName() {
        return "locations";
    }

    @Override
    public String getDescription() {
        return "Only use this function when the user's intention is getting the location (or address) of a specific place. "
            + " Never use this function to search for attractions, shops or restaurants.";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        FunctionProperty cityProperty = FunctionProperty.builder()
                .description("The Chinese name of the city of the place to query.")
                .build();
        FunctionProperty placeProperty = FunctionProperty.builder()
                .description("The place to query in Chinese.")
                .build();
        Map<String, FunctionProperty> properties = new HashMap<>();
        properties.put(PARAM_CITY, cityProperty);
        properties.put(PARAM_PLACE, placeProperty);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_CITY);
        required.add(PARAM_PLACE);
        return required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call) {
        try{
            String place = call.getParsedArguments().get(PARAM_PLACE);
            String city = call.getParsedArguments().get(PARAM_CITY);
            List<Place> places = locationService.queryPlace(place, city); 
            if(places == null || places.isEmpty()){
                return onEmptyResult(user);
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
            return onResult(user, JsonUtils.fromList(locations));
        }catch(Exception e){
            return onToolError(user);
        }
    }

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
}
