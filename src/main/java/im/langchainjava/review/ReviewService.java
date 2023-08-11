package im.langchainjava.review;

import java.util.List;

import im.langchainjava.review.model.Location;
import im.langchainjava.review.model.LocationDetail;
import im.langchainjava.review.model.Review;

public interface ReviewService {
    
    public static enum CategoryEnum{
        hotels,attractions, restaurants, geos
    } 

    List<Location> searchLocations(String query, CategoryEnum category, String address, String phone);

    LocationDetail getLocationDetails(String locationId);

    List<Review> getLocationReviews(String locationId);
}
