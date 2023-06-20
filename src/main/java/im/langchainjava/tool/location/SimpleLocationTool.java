package im.langchainjava.tool.location;

import java.lang.reflect.Array;
import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.location.LocationService;
import im.langchainjava.location.LocationService.Place;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.utils.StringUtil;

public class SimpleLocationTool extends BasicTool{

    public static int NUM_RESULT = 5;

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
    public String getToolName() {
        return "locations";
    }

    @Override
    public String getDescription() {
        return "only use this tool when the user's intention is getting the location (or address) of a specific place. "
            + " Never use this tool to find the names of attractions, shops or restaurants.";
    }


    @Override
    public String getInputFormat() {
        return "`Action Input` is the Chinese name of the place followed by comma and city in Chinese, example: `Action Input:故宫,北京`.";
    }

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String rawQueryStr = String.valueOf(action.getInput()) + "\n";
        String query = rawQueryStr.substring(0, rawQueryStr.indexOf("\n")).replace("\"", "");
        String[] queries = query.split(",");
        if(Array.getLength(queries) != 2){
            return invalidInputFormat(user);
        }
        try{

            List<Place> places = locationService.queryPlace(queries[0], queries[1]); 
            String resp = "Could not find any location.";
            if(places == null || places.isEmpty()){
                return onEmptyResult(user);
            }
            resp = "The following location is found: ";
            int n = 0;
            for(Place p : places){
                if(n++ >= this.number){
                    break;
                }
                resp = resp + p.getName() + ", address: " ;
                if(!StringUtil.isNullOrEmpty(p.getProvince())){
                    resp = resp + p.getProvince();
                }
             
                resp = resp + p.getAddress() + ", ";
    
                if(p.getLocation() != null){
                    resp = resp + "location(latitude,longitude): (" + p.getLocation().getLat() + ", " + p.getLocation().getLng() + "), location link: " + p.getUrl();
                }
    
                resp = resp + "\n";
            }
            return onResult(user, resp);
        }catch(Exception e){
            return onToolError(user);
        }
    }

    
}
