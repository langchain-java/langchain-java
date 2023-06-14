package im.langchainjava.im;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface ImService {
    public boolean sendMessageToUser(String user, String text);
    public ImMessage onMessage(String raw);

    @Getter
    @AllArgsConstructor
    public static class ImMessage{
        ImMessageType type;
        String user;
        String message;
    }

    public static enum ImMessageType{
        subscribe,
        text,
        duplicatedMessage,
        invalidFormat,
        unsupportedMessage
    }

}
