package im.langchainjava.review.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Award {
    @JsonProperty("award_type")
    String awardType;

    String year;

    List<Category> categories;
    @JsonProperty("display_name")
    String displayName;
}
