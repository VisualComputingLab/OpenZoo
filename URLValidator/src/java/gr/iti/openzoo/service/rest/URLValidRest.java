package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.URLValidImpl;
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
@Path("url")
public class URLValidRest {

    @Context
    private UriInfo context;
    
    private static URLValidImpl impl = new URLValidImpl();

    /**
     * Creates a new instance of URLValidRest
     */
    public URLValidRest() {
    }

    /**
     * Retrieves representation of an instance of gr.iti.openzoo.service.rest.URLValidRest
     * @return an instance of org.codehaus.jettison.json.JSONObject
     */
    @GET
    @Produces("application/json")
    public JSONObject getJson(@DefaultValue("status") @QueryParam("action") String action) {
        
        return impl.get(action);
    }
}
