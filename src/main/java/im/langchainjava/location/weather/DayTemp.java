package im.langchainjava.location.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DayTemp {
    float day;
    float min;
    float max;
    float night;
    float eve;
    float morn;
}
