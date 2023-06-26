package im.langchainjava.search.google;

import java.util.HashMap;
import java.util.Map;

import im.langchainjava.search.google.dto.Search;
import im.langchainjava.utils.HttpClientUtil;
// import im.langchainjava.utils.RestUtil;

public class GoogleSearchConnector {
    
    String baseUri = "https://customsearch.googleapis.com/customsearch/v1";

    public static String LANG_EN = "lang_en";
    public static String LANG_ZH = "lang_zh-CN";
    public static String LANG_TW = "lang_zh_TW";

    public static String SAFE_ACTIVE = "active";
    public static String SAFE_OFF = "off";

    public Search search(String key, String engineId, String query, int number){
        return search(key, engineId, query, 1, number, "d20", null, null, null);
    }

    public Search search(String key, String engineId, String query, int start, int number, String dateRestrict, String lang, String exactTerms, String excludeTerms){
        String url = baseUri;
        Map<String, String> params = new HashMap<>();
        params.put("cx", engineId);
        params.put("key", key);
        params.put("q", query); 
        params.put("start", String.valueOf(start));
        params.put("safe", SAFE_ACTIVE);
        params.put("num", String.valueOf(number));
        params.put("lr", LANG_ZH);
        if(lang != null){
            params.put("lr", lang);
        }
        if(dateRestrict != null){
            params.put("dateRestrict", dateRestrict);
        }
        if(exactTerms != null){
            params.put("exactTerms", String.valueOf(exactTerms));
        }
        if(excludeTerms != null){
            params.put("excludeTerms", String.valueOf(excludeTerms));
        }
        return HttpClientUtil.get(url, params, Search.class);
    }

}
