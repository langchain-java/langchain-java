package im.langchainjava.location.baidu;

import static im.langchainjava.location.baidu.BaiduMapConnector.STATUS_SUCCESS;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.location.LocationService;
import im.langchainjava.location.baidu.dto.BaiduMapPlaceChild;
import im.langchainjava.location.baidu.dto.BaiduPlaceDetail;
import im.langchainjava.location.baidu.dto.BaiduPlaceResult;
import im.langchainjava.location.baidu.dto.BaiduPlaceSuggestion;
import im.langchainjava.utils.JsonUtils;

public class BaiduMapService implements LocationService{

    BaiduMapConnector connector;

    String ak;

    public BaiduMapService(BaiduMapConnector c, String key){
        this.connector = c;
        this.ak = key;
    }

    private static String JSON = "json";

    @Override
    public List<Place> queryPlace(String query, String city) {
        String cityQuery = "";
        if(city != null){
            cityQuery = city.trim();
        }

        String suggestString = connector.getPlaceSuggestions(query, cityQuery, true, ak, JSON);
        BaiduPlaceSuggestion suggest = JsonUtils.toObject(suggestString, BaiduPlaceSuggestion.class);
        List<Place> places = new ArrayList<>();
        if(suggest!=null && suggest.getStatus() == STATUS_SUCCESS && suggest.getResult() != null){
            for(BaiduPlaceResult r : suggest.getResult()){
                if(!r.getCity().startsWith(cityQuery) && !cityQuery.startsWith(r.getCity())){
                    continue;
                }

                List<Child> children = new ArrayList<>();
                if(r.getChildren() != null){
                    for(BaiduMapPlaceChild bdc : r.getChildren()){
                        Child c = new Child(bdc.getName(), bdc.getShowName());
                        children.add(c);
                    }
                }

                String detailStr = connector.getPlaceDetail(r.getUid(), JSON, 2, ak);
                BaiduPlaceDetail detail = JsonUtils.toObject(detailStr, BaiduPlaceDetail.class);
                Place p = new Place();

                if(detail != null && detail.getResult() != null && detail.getResult().getDetailInfo() != null && detail.getResult().getDetailInfo().getDetailUrl() != null){
                    p.setUrl(detail.getResult().getDetailInfo().getDetailUrl());
                }
                p.setUid(r.getUid());
                p.setAddress(r.getAddress());
                p.setCity(r.getCity());
                p.setDistrict(r.getDistrict());
                p.setLocation(r.getLocation()); 
                p.setChildren(children);
                p.setProvince(r.getProvince());
                p.setName(r.getName());
                p.setTag(r.getTag());
                places.add(p);
            }
        }
        return places;
    }

}
