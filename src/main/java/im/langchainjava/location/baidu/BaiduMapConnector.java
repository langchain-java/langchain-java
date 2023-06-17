package im.langchainjava.location.baidu;

import im.langchainjava.location.baidu.dto.BaiduPlaceDetail;
import im.langchainjava.location.baidu.dto.BaiduPlaceSuggestion;

public interface BaiduMapConnector {

    public BaiduPlaceSuggestion getPlaceSuggestions(String query, String region, Boolean cityLimit, String ak, String output);

    public BaiduPlaceDetail getPlaceDetail(String uid, String output, int scope, String ak);
}