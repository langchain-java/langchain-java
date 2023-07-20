package im.langchainjava.location.weather;

import java.util.List;

public interface WeatherService {
    CurrentWeather getCurrentWeather(String place, String city);
    List<DailyWeather> getDailyWeather(String place,String city); 
    List<WeatherAlert> getWeatherAlerts(String place, String city);
    Weather getWeather(String place, String city);
}
