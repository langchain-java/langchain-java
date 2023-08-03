package im.langchainjava.review.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    String city;
    String country;
    @JsonProperty("postalcode")
    String postCode;
    @JsonProperty("address_string")
    String address;
}
