package im.langchainjava.review.tripadvisor;

import java.util.List;

import im.langchainjava.review.ReviewService;
import im.langchainjava.review.model.Location;
import im.langchainjava.review.model.LocationDetail;
import im.langchainjava.review.model.Review;
import im.langchainjava.review.tripadvisor.dto.LocationReviews;
import im.langchainjava.review.tripadvisor.dto.SearchResult;
import lombok.NonNull;

public class TripadvisorReviewService implements ReviewService{

    final String key;
    TripadvisorConnector connector;

    public TripadvisorReviewService(String key){
        this.key = key;
        this.connector = new TripadvisorConnector();

    }

    @Override
    public List<Location> searchLocations(@NonNull String query, Category category, String address, String phone) {
        try{
            SearchResult result = connector.searchLocation(query, category.name(), address, phone, null, this.key);
            if(result != null){
                return result.getData();
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }

    @Override
    public LocationDetail getLocationDetails(@NonNull String locationId) {
        try{
            return connector.getLocationDetails(locationId, null, null, this.key);
        }catch(Exception e){
            return null;
        }
    }

    @Override
    public List<Review> getLocationReviews(@NonNull String locationId) {
        try{
            LocationReviews result = connector.getLocationReviews(locationId, null, this.key);
            if(result != null){
                return result.getData();
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }
    
}
