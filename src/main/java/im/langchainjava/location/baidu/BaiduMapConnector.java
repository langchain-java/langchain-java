package im.langchainjava.location.baidu;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri="https://api.map.baidu.com/place/v2")
public interface BaiduMapConnector {

    public static int STATUS_SUCCESS = 0;

    @GET
    @Path("/suggestion")
    @Produces(MediaType.APPLICATION_JSON)
    String getPlaceSuggestions(@QueryParam("query") String query, @QueryParam("region") String region, @QueryParam("city_limit") @DefaultValue("false") Boolean cityLimit, @QueryParam("ak") String ak, @QueryParam("output") @DefaultValue("json") String output);

    @GET
    @Path("/detail")
    @Produces(MediaType.APPLICATION_JSON)
    String getPlaceDetail(@QueryParam("uid") String uid, @QueryParam("output") String output, @QueryParam("scope") int scope, @QueryParam("ak") String ak);
}