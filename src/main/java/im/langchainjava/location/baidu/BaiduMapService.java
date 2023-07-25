package im.langchainjava.location.baidu;

import static im.langchainjava.location.baidu.BaiduMapConnectorImpl.STATUS_SUCCESS;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.location.LocationService;
import im.langchainjava.location.baidu.dto.BaiduMapPlaceChild;
import im.langchainjava.location.baidu.dto.BaiduPlaceDetail;
import im.langchainjava.location.baidu.dto.BaiduPlaceResult;
import im.langchainjava.location.baidu.dto.BaiduPlaceSuggestion;
import im.langchainjava.utils.StringUtil;

public class BaiduMapService implements LocationService{

    BaiduMapConnector connector;

    String ak;

    public BaiduMapService(String key){
        this.connector = new BaiduMapConnectorImpl();
        this.ak = key;
    }

    private static String JSON = "json";

    @Override
    public List<Place> queryPlaceWithDetail(String query, String city) {
        List<Place> places = queryPlace(query, city);

        if(places != null){
            for(Place p : places){
                try{
                    BaiduPlaceDetail detail = connector.getPlaceDetail(p.getUid(), JSON, 2, ak);
                    if(detail != null && detail.getResult() != null && detail.getResult().getDetailInfo() != null && detail.getResult().getDetailInfo().getDetailUrl() != null){
                        p.setUrl(detail.getResult().getDetailInfo().getDetailUrl());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        return places;
    }

    @Override
    public List<Place> queryPlace(String query, String region) {
        String cityStr = region;
        String queryStr = query;
        if(!StringUtil.isNullOrEmpty(region)){
            cityStr = region.trim();
        }
        // else{
        //     region = query;
        // }

        if(StringUtil.isNullOrEmpty(query)){
            queryStr = "政府";
        }

        BaiduPlaceSuggestion suggest = null;
        List<Place> places = new ArrayList<>();
        try{
            suggest = connector.getPlaceSuggestions(queryStr, cityStr, false, ak, JSON);
        }catch(Exception e){
            return places;
        }

        if(suggest!=null && suggest.getStatus() == STATUS_SUCCESS && suggest.getResult() != null){
            for(BaiduPlaceResult r : suggest.getResult()){
                if(!r.getCity().startsWith(cityStr) && !cityStr.startsWith(r.getCity())){
                    continue;
                }

                List<Child> children = new ArrayList<>();
                if(r.getChildren() != null){
                    for(BaiduMapPlaceChild bdc : r.getChildren()){
                        Child c = new Child(bdc.getName(), bdc.getShowName());
                        children.add(c);
                    }
                }
                Place p = new Place();
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
