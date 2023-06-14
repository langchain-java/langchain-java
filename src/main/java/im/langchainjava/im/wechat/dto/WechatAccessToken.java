package im.langchainjava.im.wechat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatAccessToken {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("expires_in")
    long expiresIn;
}
