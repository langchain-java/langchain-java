package im.langchainjava.review.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessHour {
    List<Period> periods;
    @JsonProperty("weekday_text") 
    List<String> weekdayText;
} 
