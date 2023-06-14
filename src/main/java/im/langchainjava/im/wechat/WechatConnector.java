package im.langchainjava.im.wechat;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import im.langchainjava.im.wechat.dto.WechatAccessToken;
import im.langchainjava.im.wechat.dto.WechatOutTextMessage;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@RegisterRestClient(baseUri = "https://api.weixin.qq.com/cgi-bin")
public interface WechatConnector {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    WechatAccessToken getAccessToken(@QueryParam("grant_type") String grantType, @QueryParam("appid") String appId, @QueryParam("secret") String secret);

    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cgi-bin/message/custom/send")
    String sendMessage(@QueryParam("access_token") String token, WechatOutTextMessage body);
}