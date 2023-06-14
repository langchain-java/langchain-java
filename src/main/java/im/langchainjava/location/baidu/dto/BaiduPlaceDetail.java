package im.langchainjava.location.baidu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaiduPlaceDetail {
    Integer status;
    String message;
    BaiduPlaceDetailResult result;
}
