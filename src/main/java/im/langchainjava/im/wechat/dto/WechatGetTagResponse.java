package im.langchainjava.im.wechat.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatGetTagResponse {
    List<WechatTag> tags;
}
