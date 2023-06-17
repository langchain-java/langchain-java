package im.langchainjava.im.wechat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import im.langchainjava.im.ImService;
import im.langchainjava.im.wechat.dto.WechatAccessToken;
import im.langchainjava.im.wechat.dto.WechatInTextMessage;
import im.langchainjava.im.wechat.dto.WechatOutTextMessage;
import im.langchainjava.im.wechat.dto.WechatSendMsgResponse;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WechatService implements ImService{

    final private static XmlMapper xmlMapper = new XmlMapper();
    final private static String GRANT_TYPE = "client_credential";
    final private static String EVENT_SUBSCRIBE = "subscribe";

    WechatConnector connector;

    String appId;

    String secret;

    private WechatAccessToken token = null;
    private long lastRefresh = 0L;


    final private Map<String, Long> messageIdMap = new HashMap<>();

    public WechatService(String appId, String secret){
        this.connector = new WechatConnectorImpl();
        this.appId = appId;
        this.secret = secret;
    }

    public String getAccessToken(){
        long now = System.currentTimeMillis();
        if(token == null || now - lastRefresh >= (token.getExpiresIn() - 3600L) * 1000L){
            WechatAccessToken newToken;
            newToken = connector.getAccessToken(GRANT_TYPE, appId, secret);
            lastRefresh = now;
            token = newToken;
        }
        return token.getAccessToken();
    }

    public boolean sendMessageToUser(String uid, String text){

        WechatSendMsgResponse resp = connector.sendMessage(getAccessToken(), WechatOutTextMessage.createMessage(uid, text));
        log.info(JsonUtils.fromObject(resp));
        
        return true;
    }

    @Override
    public ImMessage onMessage(String raw) {
        WechatInTextMessage message;
        try {
            message = xmlMapper.readValue(raw, WechatInTextMessage.class);
            if(message.getEvent() != null && EVENT_SUBSCRIBE.equals(message.getEvent())){
                String user = message.getFromUser();
                return new ImMessage(ImMessageType.subscribe, user, message.getText());
            }

            String user = message.getFromUser();
            if(message.getText() == null){
                return new ImMessage(ImMessageType.unsupportedMessage, user, message.getText());
            }else{
                Long messageId = message.getMessageId();
                Long oldMessageId = messageIdMap.putIfAbsent(user, messageId);
                messageIdMap.put(user, messageId);
                if(oldMessageId!= null && Objects.equals(oldMessageId,messageId)){
                    return new ImMessage(ImMessageType.duplicatedMessage, user, message.getText());
                }
                return new ImMessage(ImMessageType.text, user, message.getText());
            }
        } catch (JsonProcessingException e) {
            return new ImMessage(ImMessageType.invalidFormat, null, null);
        }
    }


}
