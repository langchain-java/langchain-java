package im.langchainjava.tool.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.location.weather.DailyWeather;
import im.langchainjava.location.weather.Weather;
import im.langchainjava.location.weather.WeatherNarr;
import im.langchainjava.location.weather.WeatherService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolUtils;
import im.langchainjava.utils.DateTimeUtils;
import im.langchainjava.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeatherForecastTool extends BasicTool{

    public static String PARAM_PLACE = "place";
    public static String PARAM_CITY = "city";
    public static String PARAM_DATE = "date";

    public static long TIME_ONEDAY_SEC = 24 * 60 * 60;

    ImService wechat;

    WeatherService weatherService;

    // LlmService llm;

    // int number;

    public WeatherForecastTool(ImService wechat, WeatherService weatherService, LlmService llm){
        // super(memory);
        this.wechat = wechat;
        this.weatherService = weatherService;
        // this.llm = llm;
    }

    // public CurrentWeatherTool numberOfResults(int num){
    //     this.number = num;
    //     return this;
    // }

    @Override
    public String getName() {
        return "get_weather_for_given_date_of_given_place";
    }

    @Override
    public String getDescription() {
        return  "Get weather forecast for a given date of a given place.";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        FunctionProperty cityProperty = FunctionProperty.builder()
                .description("The Chinese name of the city of the place to query. Could not be empty or null.")
                .build();
        FunctionProperty placeProperty = FunctionProperty.builder()
                .description("The place (in Chinese) to look up its address. Could not be empty or null.")
                .build();
        FunctionProperty dateProperty = FunctionProperty.builder()
                .description("The date of the weather forecast. Must be in formate `yyyyMMdd`. Example is: 20220718")
                .build();
        Map<String, FunctionProperty> properties = new HashMap<>();
        properties.put(PARAM_CITY, cityProperty);
        properties.put(PARAM_PLACE, placeProperty);
        properties.put(PARAM_DATE, dateProperty);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_CITY);
        required.add(PARAM_PLACE);
        required.add(PARAM_DATE);
        return required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        try{
            String place = ToolUtils.getStringParam(call, PARAM_PLACE);
            String city = ToolUtils.getStringParam(call, PARAM_CITY);

            if(StringUtil.isNullOrEmpty(city)){
                return invalidParameter(user, "function input " + PARAM_CITY + " can not be empty.");
            }
            // if(StringUtil.isNullOrEmpty(place)){
            //     return invalidParameter(user, "function input " + PARAM_PLACE + " can not be empty.");
            // }
            String date = ToolUtils.getStringParam(call, PARAM_DATE);
            if(StringUtil.isNullOrEmpty(date)){
                return invalidParameter(user, "function input " + PARAM_DATE + " can not be empty.");
            }
            
            String query = city + place + " " + date;
            Weather weather = weatherService.getWeather(place, city);
            wechat.sendMessageToUser(user, "[天气预报]\n正在查找 " + query + " 的天气情况。"); 
            StringBuilder sb = new StringBuilder();
            if(weather != null && weather.getDaily() != null && !weather.getDaily().isEmpty()){
                List<DailyWeather> daily = weather.getDaily();
                int timezoneOffset = weather.getTimezoneOffsetSec();
                int diff = DateTimeUtils.getDayDiff(date, timezoneOffset);
                if(diff >= daily.size() || diff < 0){
                    String msg = "[天气预报]" + query + "\n超出天气预报范围（仅支持未来7天）。";
                    wechat.sendMessageToUser(user, msg); 
                    return onEmptyResult(user, msg);
                }

                DailyWeather w = daily.get(diff);

                sb.append("[天气预报]").append(query).append("\n")
                    .append("气温：").append(w.getTemp().getMin()).append("~").append(w.getTemp().getMax()).append("摄氏度\n")
                    .append("体感气温：").append(w.getFeelsLike().getNight()).append("~").append(w.getFeelsLike().getDay()).append("摄氏度\n")
                    .append("气压：").append(w.getPressure()).append("hPa\n")
                    .append("湿度：").append(w.getHumidity()).append("%\n")
                    .append("风速：").append(w.getWindSpeed()).append("米/秒\n");
                if(w.getVisibility() > 0){
                    sb.append("能见度：").append(w.getVisibility()).append("米\n");
                }
                if(w.getWeather() != null && !w.getWeather().isEmpty()){
                    sb.append("天气情况：");
                    for(WeatherNarr ww : w.getWeather()){
                        sb.append(ww.getDescription()).append("。");
                    }
                }
                String msg = sb.toString();
                wechat.sendMessageToUser(user, msg);
                return onResult(user, msg);
            }
            
            wechat.sendMessageToUser(user, "[天气预报]" + query + "\n没有找到任何结果。"); 
            return onEmptyResult(user);

        }catch(Exception e){
            return onToolError(user);
        }
    }

}
