package im.langchainjava.location;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface LocationService {
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location{
        String lat;
        String lng;
    }

    @AllArgsConstructor
    @Data
    public static class Child{
        String name;
        String displayName;
    }

    @NoArgsConstructor
    @Data
    public static class Place{ 
        String uid;
        String name;
        Location location;
        List<Child> children;
        String city;
        String district;
        String province;
        String address;
        String tag;
        String url;
    }

    public List<Place> queryPlace(String query, String city);

}
