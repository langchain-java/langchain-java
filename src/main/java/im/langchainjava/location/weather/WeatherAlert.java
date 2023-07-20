package im.langchainjava.location.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherAlert {
    @JsonProperty("sender_name")
    String sender;

    String event;

    @JsonProperty("start")
    long startTimeSec;

    @JsonProperty("end")
    long endTimeSec;

    String description;
}
