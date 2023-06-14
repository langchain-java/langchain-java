package im.langchainjava.location.baidu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiduMapPlaceChild{
    String uid;
    @JsonProperty("show_name")
    String showName;
    String name;
}