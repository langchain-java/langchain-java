package im.langchainjava.review.tripadvisor.dto;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import im.langchainjava.review.model.Review;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationReviews {
    List<Review> data;
}
