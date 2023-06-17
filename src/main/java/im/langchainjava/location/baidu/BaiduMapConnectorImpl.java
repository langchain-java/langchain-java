package im.langchainjava.location.baidu;

import java.util.HashMap;
import java.util.Map;

import im.langchainjava.location.baidu.dto.BaiduPlaceDetail;
import im.langchainjava.location.baidu.dto.BaiduPlaceSuggestion;
import im.langchainjava.utils.RestUtil;

public class BaiduMapConnectorImpl implements BaiduMapConnector {

    String baseUri = "https://api.map.baidu.com/place/v2";

    public static int STATUS_SUCCESS = 0;

    public BaiduPlaceSuggestion getPlaceSuggestions(String query, String region, Boolean cityLimit, String ak, String output){
        String url = baseUri + "/suggestion";
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("region", region);
        params.put("city_limit", String.valueOf(cityLimit));
        params.put("ak", ak);
        params.put("output", "json");

        return RestUtil.get(url, params, BaiduPlaceSuggestion.class);
    }


    public BaiduPlaceDetail getPlaceDetail(String uid, String output, int scope, String ak){
        String url = baseUri + "/detail";
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);
        params.put("scope", String.valueOf(scope));
        params.put("ak", ak);
        params.put("output", "json");
        return RestUtil.get(url, params, BaiduPlaceDetail.class);
    }
}