package im.langchainjava.tool.location;

import java.lang.reflect.Array;
import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.location.LocationService;
import im.langchainjava.location.LocationService.Place;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.Tool;
import im.langchainjava.utils.StringUtil;

public class SimpleLocationTool implements Tool{

    ImService wechat;

    LocationService locationService;

    LlmService llm;

    String desc;

    public SimpleLocationTool(ImService wechat, LocationService location, LlmService llm){
        this.wechat = wechat;
        this.locationService = location;
        this.llm = llm;
        this.desc = null;
    }

    public SimpleLocationTool(ImService wechat, LocationService location, LlmService llm, String desc){
        this.wechat = wechat;
        this.locationService = location;
        this.llm = llm;
        this.desc = desc;
    }

    @Override
    public String getToolName() {
        return "locations";
    }

    @Override
    public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return " only use this tool when the user's intention is getting the location (or address) of a specific place. "
            + " Never use this tool to find the names of attractions, shops or restaurants. "
            + " Action Input is the Chinese name of the place followed by comma and city in Chinese, example: `Action Input:故宫,北京`.";
    }

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String rawQueryStr = String.valueOf(action.getInput()) + "\n";
        String query = rawQueryStr.substring(0, rawQueryStr.indexOf("\n")).replace("\"", "");
        String[] queries = query.split(",");
        if(Array.getLength(queries) != 2){
            return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, "Invalid input, the input must be the name of the place followed by comma and city, example: `故宫,北京`.")
                        .message(Tool.KEY_THOUGHT, " I have had wrong input format. I should try this tool again with a corrected input format.")
                        .sync();
        }
        try{

            List<Place> places = locationService.queryPlace(queries[0], queries[1]); 
            String resp = "Could not find any location.";
            if(places == null || places.isEmpty()){
                return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, resp)
                        .message(Tool.KEY_THOUGHT, " There is no result. I should try another tool or tell the user `我找不到结果`.")
                        .sync();
            }
            resp = "The following location is found: ";
            for(Place p : places){
    
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
            return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, resp)
                        .message(Tool.KEY_THOUGHT, " Now I have the results. I should inform the user with the location and location link I found.")
                        .sync();
        }catch(Exception e){
            return ToolOuts.of(user, true)
                        .message(Tool.KEY_OBSERVATION, "This tool is not available. Don't use this tool again.")
                        .message(Tool.KEY_THOUGHT, " I should try another tool or tell the user `我找不到结果`.")
                        .sync();
        }
    }

    @Override
    public void onClearedMemory(String user) {
    }
    
}
