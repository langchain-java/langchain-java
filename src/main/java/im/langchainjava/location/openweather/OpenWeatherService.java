package im.langchainjava.location.openweather;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.location.LocationService;
import im.langchainjava.location.LocationService.Location;
import im.langchainjava.location.LocationService.Place;
import im.langchainjava.location.weather.CurrentWeather;
import im.langchainjava.location.weather.DailyWeather;
import im.langchainjava.location.weather.Weather;
import im.langchainjava.location.weather.WeatherAlert;
import im.langchainjava.location.weather.WeatherService;

public class OpenWeatherService implements WeatherService{

    OpenWeatherConnector connector;
    LocationService locationService;

    String appId;
    String language;

    public OpenWeatherService(String appId, LocationService locationService){
        this.appId = appId;
        this.connector = new OpenWeatherConnector();
        this.locationService = locationService;
        this.language = OpenWeatherConnector.ZH_CN;
    }

    public OpenWeatherService(String appId, LocationService locationService, String lang){
        this.appId = appId;
        this.connector = new OpenWeatherConnector();
        this.locationService = locationService;
        this.language = lang;
    }

    @Override
    public CurrentWeather getCurrentWeather(String place, String city) {
        Weather w = getWeatherOfPlace(place, city, null);
        if(w == null){
            return null;
        }
        return w.getCurrent();
    }

    @Override
    public List<DailyWeather> getDailyWeather(String place, String city) {
        Weather w = getWeatherOfPlace(place, city, null);
        if(w == null){
            return new ArrayList<>();
        }
        return w.getDaily();
    }

    @Override
    public List<WeatherAlert> getWeatherAlerts(String place, String city) {
        Weather w = getWeatherOfPlace(place, city, null);
        if(w == null){
            return new ArrayList<>();
        }
        return w.getAlerts();
    }
    
    private Weather getWeatherOfPlace(String place, String city, String lang){
        String myLang = lang == null ? this.language : lang;
        List<Place> places = this.locationService.queryPlace(place, city);
        if(places != null && !places.isEmpty()){
            Location l = null;
            for(Place p : places){
                l = p.getLocation();
                if(l != null){
                    break;
                }
            }
            
            if(l == null){
                return null;
            }

            Weather weather = connector.getWeather(Float.valueOf(l.getLat()), Float.valueOf(l.getLng()), appId, myLang);
            return weather;
        }
        return null;
    }

    @Override
    public Weather getWeather(String place, String city) {
        return getWeatherOfPlace(place, city, null);
    }
}
