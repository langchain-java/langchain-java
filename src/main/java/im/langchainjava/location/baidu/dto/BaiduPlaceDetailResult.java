package im.langchainjava.location.baidu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import im.langchainjava.location.LocationService.Location;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaiduPlaceDetailResult {
    String uid;
    @JsonProperty("street_id")
    String streetId;
    String name;
    @JsonProperty("result_type")
    String resultType;
    Location location;
    String address;
    String province;
    String city;
    String area;
    @JsonProperty("detail_info")
    BaiduPlaceDetailInfo detailInfo;
}
