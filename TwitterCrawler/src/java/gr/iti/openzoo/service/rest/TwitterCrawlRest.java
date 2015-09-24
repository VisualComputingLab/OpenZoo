package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.TwitterCrawlImpl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONObject;

/**
 * REST Web Service
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@Path("crawl")
public class TwitterCrawlRest {

    @Context
    private UriInfo context;
    
    private static TwitterCrawlImpl impl = new TwitterCrawlImpl();

    /**
     * Creates a new instance of TwitterCrawlRest
     */
    public TwitterCrawlRest() {
    }

    /**
     * Retrieves representation of an instance of gr.iti.openzoo.service.rest.TwitterCrawlRest
     * @return an instance of org.codehaus.jettison.json.JSONObject
     */
    @GET
    @Produces("application/json")
    public JSONObject getJson(@DefaultValue("status") @QueryParam("action") String action) {
        
        return impl.get(action);
    }
}
