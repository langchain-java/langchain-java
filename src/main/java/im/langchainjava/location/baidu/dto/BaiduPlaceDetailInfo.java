package im.langchainjava.location.baidu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import im.langchainjava.location.LocationService.Location;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaiduPlaceDetailInfo {

    String tag;
    @JsonProperty("navi_location")
    Location naviLocation;
    @JsonProperty("new_catalog")
    String newCatalog;
    @JsonProperty("shop_hours")
    String shopHours;
    @JsonProperty("detail_url")
    String detailUrl;
}
