package im.langchainjava.review.tripadvisor;

import java.util.HashMap;
import java.util.Map;

import im.langchainjava.review.model.LocationDetail;
import im.langchainjava.review.tripadvisor.dto.LocationReviews;
import im.langchainjava.review.tripadvisor.dto.SearchResult;
import im.langchainjava.utils.HttpClientUtil;
import im.langchainjava.utils.StringUtil;

final public class TripadvisorConnector {

    public static String LANG_ZH = "zh";
    public static String CURRENCY_CNY = "CNY";

    private static String url = "https://api.content.tripadvisor.com/api/v1/location/";

    public SearchResult searchLocation(String query, String category, String address, String phone, String lang, String key){
        String myUrl = url + "search";
        String myLang = LANG_ZH;
        if(StringUtil.isNullOrEmpty(lang)){
            myLang = lang;
        }
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("searchQuery", query);
        params.put("category", category);
        params.put("address", address);
        params.put("phone", phone);
        params.put("language", myLang);
        return HttpClientUtil.get(myUrl, params, SearchResult.class);
    }

    public LocationDetail getLocationDetails(String locationId, String currency, String lang, String key){
        String myUrl = url + locationId + "/details";
        String myLang = LANG_ZH;
        if(StringUtil.isNullOrEmpty(lang)){
            myLang = lang;
        }
        String myCurr = CURRENCY_CNY;
        if(StringUtil.isNullOrEmpty(currency)){
            myCurr = currency;
        }
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("language", myLang);
        params.put("currency", myCurr);

        return HttpClientUtil.get(myUrl, params, LocationDetail.class);
    }

    public LocationReviews getLocationReviews(String locationId, String lang, String key){
        String myUrl = url + locationId + "/reviews";
        String myLang = LANG_ZH;
        if(StringUtil.isNullOrEmpty(lang)){
            myLang = lang;
        }

        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("language", myLang);

        return HttpClientUtil.get(myUrl, params, LocationReviews.class);
    }



}
