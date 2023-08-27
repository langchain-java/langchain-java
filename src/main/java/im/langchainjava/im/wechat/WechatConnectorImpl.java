package im.langchainjava.im.wechat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import im.langchainjava.im.wechat.dto.WechatAccessToken;
import im.langchainjava.im.wechat.dto.WechatGetTagResponse;
import im.langchainjava.im.wechat.dto.WechatOutTextMessage;
import im.langchainjava.im.wechat.dto.WechatSendMsgResponse;
import im.langchainjava.im.wechat.dto.WechatTag;
import im.langchainjava.im.wechat.dto.WechatTagUserRequest;
import im.langchainjava.utils.HttpClientUtil;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WechatConnectorImpl implements WechatConnector {

    String baseUri = "https://api.weixin.qq.com/cgi-bin";

    @Override
    public WechatAccessToken getAccessToken(String grantType, String appId, String secret){
        String url = baseUri + "/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", grantType);
        params.put("appid", appId);
        params.put("secret", secret);
        return HttpClientUtil.get(url, params, WechatAccessToken.class);
    }

    
    @Override
    public WechatSendMsgResponse sendMessage(String token, WechatOutTextMessage body){
        String url = baseUri + "/message/custom/send";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", token);
        return HttpClientUtil.post(url, params,body, WechatSendMsgResponse.class);

    }


    @Override
    public WechatGetTagResponse getTags(String token) {
        String url = baseUri + "/tags/get";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", token);
        return HttpClientUtil.get(url, params, WechatGetTagResponse.class);
    }


    @Override
    public WechatSendMsgResponse tagUser(String token, String user, WechatTag tag) {
        String url = baseUri + "/tags/members";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", token);
        WechatTagUserRequest request = new WechatTagUserRequest(Collections.singletonList(user), tag.getId());
        return HttpClientUtil.post(url, params, request, WechatSendMsgResponse.class);
    }
}