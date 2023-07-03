package im.langchainjava.location.baidu.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


import im.langchainjava.location.LocationService.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaiduPlaceResult{
    String name;
    Location location;
    String uid;
    String province;
    String city;
    String district;
    String business;
    @JsonProperty("cityid")
    String cityId;
    String tag;
    String address;
    List<BaiduMapPlaceChild> children;

    @JsonProperty("adcode")
    String adCode;
}