package im.langchainjava.location.weather;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentWeather extends BasicWeather{
    @JsonProperty("sunrise")
    long sunriseTimeSec;

    @JsonProperty("sunset")
    long sunsetTimeSec;

    float temp;

    @JsonProperty("feels_like")
    float feelsLike;

    List<WeatherNarr> weather;
}
