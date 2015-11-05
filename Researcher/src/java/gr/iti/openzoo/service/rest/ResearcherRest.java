package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.ResearcherService;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * REST Web Service
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@Path("manage")
public class ResearcherRest {

    @Context
    private UriInfo context;
    
    private final static ResearcherService impl = new ResearcherService();

    public ResearcherRest() {
    }

    @GET
    @Produces("application/json")
    public String getJson(@DefaultValue("status") @QueryParam("action") String action) {
        
        return impl.get(action).toString();
    }
}
