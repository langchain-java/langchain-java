package im.langchainjava.review.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDetail {
    @JsonProperty("location_id")
    String id;

    String name;
    String description;
    
    @JsonProperty("web_url")
    String url;

    @JsonProperty("address_obj")
    Address address;

    String latitude;
    String longitude;

    String timezone;
    String email;
    String phone;

    String website;

    @JsonProperty("write_review")
    String writeReview;

    @JsonProperty("ranking_data")
    RankingData rankingData;

    String rating;
    
    @JsonProperty("num_reviews")
    String numReviews;

    @JsonProperty("review_rating_count")
    Map<String,String> reviewRatingCount;

    Map<String, Category> subratings;

    @JsonProperty("price_level")
    String priceLeve;

    BusinessHour hours;

    Category category;

    List<Category> subcategory;

    List<Category> groups;

    @JsonProperty("trip_type")
    List<Category> tripTypes;

    List<Category> cuisine;


}
