package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.ServerStatisticsImpl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@Path("stats")
public class ServerStatisticsRest {

    @Context
    private UriInfo context;
    
    private static ServerStatisticsImpl impl = new ServerStatisticsImpl();

    /**
     * Creates a new instance of ServerStatisticsRest
     */
    public ServerStatisticsRest() {
    }

    /**
     * Retrieves representation of an instance of gr.iti.openzoo.service.rest.ServerStatisticsRest
     * @return an instance of org.codehaus.jettison.json.JSONObject
     */
    @GET
    @Produces("application/json")
    public org.codehaus.jettison.json.JSONObject getJson() {
        
        return impl.get();
    }
}
