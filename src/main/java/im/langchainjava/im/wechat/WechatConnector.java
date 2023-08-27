package im.langchainjava.im.wechat;

import im.langchainjava.im.wechat.dto.WechatAccessToken;
import im.langchainjava.im.wechat.dto.WechatGetTagResponse;
import im.langchainjava.im.wechat.dto.WechatOutTextMessage;
import im.langchainjava.im.wechat.dto.WechatSendMsgResponse;
import im.langchainjava.im.wechat.dto.WechatTag;


public interface WechatConnector {

    public WechatAccessToken getAccessToken(String grantType, String appId, String secret);
    
    public WechatSendMsgResponse sendMessage(String token, WechatOutTextMessage body);

    public WechatGetTagResponse getTags(String token);

    public WechatSendMsgResponse tagUser(String token, String user, WechatTag tag);
}