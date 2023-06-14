package im.langchainjava.utils;

public class StringUtil {
    public static boolean isNullOrEmpty(String text){
        if(text == null){
            return true;
        }
        if(text.isEmpty()){
            return true;
        }
        return false;
    }
}
