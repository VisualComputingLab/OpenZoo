package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.ResearcherService;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
    
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public String postJson(String content) {

        JSONObject input;
        try
        {
            input = new JSONObject(content);
        }
        catch (JSONException e)
        {
            System.err.println("Could not convert input to JSONObject: " + e);
            input = new JSONObject();
        }
        
        return impl.post(input).toString();
    }
}
