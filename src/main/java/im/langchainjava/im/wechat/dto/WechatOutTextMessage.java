package im.langchainjava.im.wechat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WechatOutTextMessage {
    @JsonProperty("touser")
    String toUser;

    @JsonProperty(value="msgtype",defaultValue="text")
    String messageType;

    @JsonProperty("text")
    Text text;
    
    @Data
    @AllArgsConstructor
    public
    static class Text{
        @JsonProperty("content")
        String content;
    } 

    public static WechatOutTextMessage createMessage(String toUser, String text){
        Text t = new Text(text);
        return new WechatOutTextMessage(toUser, "text", t);
    }
}
