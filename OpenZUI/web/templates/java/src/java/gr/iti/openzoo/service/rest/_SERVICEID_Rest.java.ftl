package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.${ServiceID}Impl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONObject;

/**
 * REST Web Service
 *
 * @author ${Author}
 */
@Path("${ResourcePath}")
public class ${ServiceID}Rest {

    @Context
    private UriInfo context;
    
    private static ${ServiceID}Impl impl = new ${ServiceID}Impl();

    /**
     * Creates a new instance of ${ServiceID}Rest
     */
    public ${ServiceID}Rest() {
    }

    /**
     * Retrieves representation of an instance of gr.iti.openzoo.service.rest.${ServiceID}Rest
     * @return an instance of org.codehaus.jettison.json.JSONObject
     */
    @GET
    @Produces("application/json")
    public JSONObject getJson(@DefaultValue("status") @QueryParam("action") String action) {
        
        return impl.get(action);
    }
}
