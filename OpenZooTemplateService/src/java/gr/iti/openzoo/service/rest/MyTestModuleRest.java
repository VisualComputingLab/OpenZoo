package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.MyTestModuleImpl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONObject;

/**
 * REST Web Service
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@Path("test")
public class MyTestModuleRest {

    @Context
    private UriInfo context;
    
    private static MyTestModuleImpl impl = new MyTestModuleImpl();

    /**
     * Creates a new instance of MyTestModuleRest
     */
    public MyTestModuleRest() {
    }

    @GET
    @Produces("application/json")
    public JSONObject getJson(@DefaultValue("status") @QueryParam("action") String action) {
        
        return impl.get(action);
    }
    
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public JSONObject postJson(JSONObject content) {
        
        return impl.post(content);
    }
}
