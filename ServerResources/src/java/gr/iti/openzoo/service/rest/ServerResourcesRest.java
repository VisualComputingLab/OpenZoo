package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.ServerResourcesService;
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
@Path("manage")
public class ServerResourcesRest {

    @Context
    private UriInfo context;
    
    private final static ServerResourcesService impl = new ServerResourcesService();

    public ServerResourcesRest() {
    }

    @GET
    @Produces("application/json")
    public String getJson() {
        
        return impl.get().toString();
    }
}
