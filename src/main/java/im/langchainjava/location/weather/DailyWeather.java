package im.langchainjava.location.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyWeather extends BasicWeather{
    @JsonProperty("sunrise")
    long sunriseTimeSec;
    
    @JsonProperty("sunset")
    long sunsetTimeSec;

    @JsonProperty("moonrise")
    long moonriseTimeSec;

    @JsonProperty("moonset")
    long moonsetTimeSec;

    @JsonProperty("moon_phase")
    int moonPhase;

    String summary;

    DayTemp temp;

    @JsonProperty("feels_like")
    DayFeelTemp feelsLike;

    List<WeatherNarr> weather;
}
