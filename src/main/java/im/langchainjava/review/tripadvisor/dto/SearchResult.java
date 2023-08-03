package im.langchainjava.review.tripadvisor.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import im.langchainjava.review.model.Location;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
    List<Location> data;
}
