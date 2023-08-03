package im.langchainjava.location.openweather;

import java.util.HashMap;
import java.util.Map;

import im.langchainjava.location.weather.Weather;
import im.langchainjava.utils.HttpClientUtil;

public class OpenWeatherConnector {
    private static String url = "https://api.openweathermap.org/data/3.0/onecall";
    private static String UNIT_METRIC = "metric";
    public static String ZH_CN = "zh_cn";
    public Weather getWeather(float lat, float lon, String appId, String lang){
        Map<String, String> params = new HashMap<>();
        params.put("lat", String.valueOf(lat));
        params.put("lon", String.valueOf(lon));
        params.put("appid", appId);
        params.put("units", UNIT_METRIC);
        params.put("lang", lang);
        return HttpClientUtil.get(url, params, Weather.class);
    }
}
