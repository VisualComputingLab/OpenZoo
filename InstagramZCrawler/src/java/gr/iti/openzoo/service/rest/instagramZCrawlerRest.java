/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.openzoo.service.rest;

import gr.iti.openzoo.service.impl.InstagramZCrawlerImpl;
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
 * @author dimitris.samaras
 */
@Path("crawl")
public class instagramZCrawlerRest {

    @Context
    private UriInfo context;
    
    private static InstagramZCrawlerImpl impl = new InstagramZCrawlerImpl();

    /**
     * Creates a new instance of instagramZCrawlerRest
     */
    public instagramZCrawlerRest() {
    }

    /**
     * Retrieves representation of an instance of gr.iti.openzoo.service.rest.instagramZCrawlerRest
     * @return an instance of org.codehaus.jettison.json.JSONObject
     */
    @GET
    @Produces("application/json")
    public JSONObject getJson(@DefaultValue("status") @QueryParam("action") String action)
{      
    return impl.get(action);
}

}
