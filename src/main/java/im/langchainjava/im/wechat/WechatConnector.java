package im.langchainjava.im.wechat;

import im.langchainjava.im.wechat.dto.WechatAccessToken;
import im.langchainjava.im.wechat.dto.WechatOutTextMessage;
import im.langchainjava.im.wechat.dto.WechatSendMsgResponse;


public interface WechatConnector {

    public WechatAccessToken getAccessToken(String grantType, String appId, String secret);
    
    public WechatSendMsgResponse sendMessage(String token, WechatOutTextMessage body);
}