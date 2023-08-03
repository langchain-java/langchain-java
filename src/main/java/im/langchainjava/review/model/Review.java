package im.langchainjava.review.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Review {
    @JsonProperty("published_date")
    String publishedDate;

    int rating;

    @JsonProperty("helpful_votes")
    int helpfulVotes;

    String title;

    String text;

    @JsonProperty("trip_type")
    String tripType;

    @JsonProperty("travel_date")
    String travelDate;

    User user;

    Map<String, Category> subratings;

    @JsonProperty("owner_response")
    OwnerResponse ownerResponse; 
}   
