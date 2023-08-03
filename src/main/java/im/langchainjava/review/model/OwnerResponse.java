package im.langchainjava.review.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OwnerResponse {
    String title;
    String text;
    @JsonProperty("lang")
    String language;
    String author;
    @JsonProperty("published_date")
    String publishedDate;
}
