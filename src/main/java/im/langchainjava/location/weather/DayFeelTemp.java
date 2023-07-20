package im.langchainjava.location.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DayFeelTemp {
    float day;
    float night;
    float eve;
    float morn;
}
