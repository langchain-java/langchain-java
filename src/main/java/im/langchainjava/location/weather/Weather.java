package im.langchainjava.location.weather;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
    float lat;
    float lon;
    String timezone;

    @JsonProperty("timezone_offset")
    int timezoneOffsetSec;

    CurrentWeather current;

    List<HourlyWeather> hourly;

    List<DailyWeather> daily;

    List<WeatherAlert> alerts;

}
