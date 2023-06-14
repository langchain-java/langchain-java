package im.langchainjava.im.wechat.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "xml")
public class WechatInTextMessage {
    @JacksonXmlProperty(localName = "ToUserName")
    @JacksonXmlCData
    String toUser;

    @JacksonXmlProperty(localName = "FromUserName")
    @JacksonXmlCData
    String fromUser;

    @JacksonXmlProperty(localName = "CreateTime")
    long createTime;

    @JacksonXmlProperty(localName = "MsgType")
    @JacksonXmlCData
    String messageType;

    @JacksonXmlProperty(localName = "Content")
    @JacksonXmlCData
    String text;

    @JacksonXmlProperty(localName = "MsgId")
    @JacksonXmlCData
    long messageId;
    
    @JacksonXmlProperty(localName = "MediaId")
    @JacksonXmlCData
    String mediaId;

    @JacksonXmlProperty(localName = "Format")
    @JacksonXmlCData
    String format;

    @JacksonXmlProperty(localName = "Recognition")
    @JacksonXmlCData
    String recognition;

    @JacksonXmlProperty(localName = "PicUrl")
    @JacksonXmlCData
    String picUrl;

    @JacksonXmlProperty(localName = "Event")
    @JacksonXmlCData
    String event;

    @JacksonXmlProperty(localName = "EventKey")
    @JacksonXmlCData
    String eventKey;

    @JacksonXmlProperty(localName = "Location_X")
    float locationX;

    @JacksonXmlProperty(localName = "Location_Y")
    float locationY;

    @JacksonXmlProperty(localName = "Scale")
    int scale;

    @JacksonXmlProperty(localName = "Label")
    @JacksonXmlCData
    String label;

    @JacksonXmlProperty(localName = "Title")
    @JacksonXmlCData
    String title;

    @JacksonXmlProperty(localName = "Description")
    @JacksonXmlCData
    String description;

    @JacksonXmlProperty(localName = "Url")
    @JacksonXmlCData
    String url;
}
