package im.langchainjava.im.wechat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatSendMsgResponse {
    @JsonProperty("errcode")
    Integer errorCode;
    @JsonProperty("errmsg")
    String errMsg;
}
