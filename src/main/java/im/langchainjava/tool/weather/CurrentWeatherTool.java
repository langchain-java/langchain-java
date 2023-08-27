package im.langchainjava.tool.weather;

import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.location.weather.CurrentWeather;
import im.langchainjava.location.weather.WeatherNarr;
import im.langchainjava.location.weather.WeatherService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.tool.askuser.form.FormBuilders;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrentWeatherTool extends Tool{

    public static String PARAM_PLACE = "place";
    public static String PARAM_DESC_PLACE = "The place name (in Chinese) to look up its address.";
    public static String PARAM_CITY = "city";
    public static String PARAM_DESC_CITY = "The city name (in Chinese) of the place in the query.";

    ImService im;

    WeatherService weatherService;

    LlmService llm;

    // int number;

    public CurrentWeatherTool(ImService im, WeatherService weatherService, LlmService llm){
        this.im = im;
        this.weatherService = weatherService;
        this.llm = llm;
        dependencyAndProperty(im, FormBuilders.cityForm(llm, PARAM_CITY, PARAM_DESC_CITY));
        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_PLACE, PARAM_DESC_PLACE));
    }

    // public CurrentWeatherTool numberOfResults(int num){
    //     this.number = num;
    //     return this;
    // }

    @Override
    public String getName() {
        return "get_current_weather_of_place";
    }

    @Override
    public String getDescription() {
        return  "Get the current weather of a given place.";
    }

    // @Override
    // public Map<String, FunctionProperty> getProperties() {
    //     FunctionProperty cityProperty = FunctionProperty.builder()
    //             .description("The Chinese name of the city of the place to query. Could not be blank, empty or null.")
    //             .build();
    //     FunctionProperty placeProperty = FunctionProperty.builder()
    //             .description("The place (in Chinese) to look up its address. Could not be blank, empty or null.")
    //             .build();
    //     Map<String, FunctionProperty> properties = new HashMap<>();
    //     properties.put(PARAM_CITY, cityProperty);
    //     properties.put(PARAM_PLACE, placeProperty);
    //     return properties;
    // }

    // @Override
    // public List<String> getRequiredProperties() {
    //     List<String> required = new ArrayList<>();
    //     required.add(PARAM_CITY);
    //     required.add(PARAM_PLACE);
    //     return required;
    // }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        try{
            String place = ToolUtils.getStringParam(call, PARAM_PLACE);
            String city = ToolUtils.getStringParam(call, PARAM_CITY);
            String query = city + place;
            im.sendMessageToUser(user, "[当前天气]\n正在查找" + query + "的天气情况。"); 

            if(StringUtil.isNullOrEmpty(city)){
                return ToolOuts.invalidParameter(user, "function input " + PARAM_CITY + " can not be empty.");
            }
            // if(StringUtil.isNullOrEmpty(place)){
            //     return invalidParameter(user, "function input " + PARAM_PLACE + " can not be empty.");
            // }
            CurrentWeather weather = weatherService.getCurrentWeather(place, city);
            StringBuilder sb = new StringBuilder();
            if(weather != null){
                sb.append("[当前天气]").append(city).append(place).append("\n")
                    .append("气温：").append(weather.getTemp()).append("摄氏度\n")
                    .append("体感气温：").append(weather.getFeelsLike()).append("摄氏度\n")
                    .append("气压：").append(weather.getPressure()).append("hPa\n")
                    .append("湿度：").append(weather.getHumidity()).append("%\n")
                    .append("能见度：").append(weather.getVisibility()).append("米\n")
                    .append("风速：").append(weather.getWindSpeed()).append("米/秒\n");
                if(weather.getWeather() != null && !weather.getWeather().isEmpty()){
                    sb.append("气象：");
                    for(WeatherNarr w : weather.getWeather()){
                        sb.append(w.getDescription()).append("。");
                    }
                }
                String msg = sb.toString();
                im.sendMessageToUser(user, msg);
                return ToolOuts.onResult(user, msg);
            }
           
            String msg = "[当前天气]" + query + "\n没有找到任何结果。";
            im.sendMessageToUser(user, msg); 
            return ToolOuts.onEmptyResult(user, msg);

        }catch(Exception e){
            return ToolOuts.onToolError(user, "调用当前天气发生错误：" + e.getMessage());
        }
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        // This method will not be used.
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public List<String> getRequiredProperties() {
        // This method will not be used.
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredProperties'");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // This method will not be used.
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }

}
