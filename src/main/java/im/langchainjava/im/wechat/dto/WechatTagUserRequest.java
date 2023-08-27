package im.langchainjava.im.wechat.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WechatTagUserRequest {
    @JsonProperty("openid_list")
    List<String> openIdList;

    @JsonProperty("tagid")
    int tagId;
}
