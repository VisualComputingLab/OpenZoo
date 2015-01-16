package gr.iti.openzoo.admin;

import gr.iti.openzoo.util.SerializationUtil;
import gr.iti.openzoo.util.Utilities;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Web application life cycle listener. Implements interface for receiving
 * notification events about ServletContext life cycle changes.
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 * @author Dimitris Samaras <dimitris.samaras@iti.gr>
 *
 */
public class OpenZooContextListener implements ServletContextListener {

    private OpenZooBeholder beholder = null;
    private String REALPATH;
    private ServletContext sc;
    private Utilities util = new Utilities();
    private ServiceParameters parameters = new ServiceParameters();
    private static String CONFIG_FILE = "config.json";
    /*
     * Receives notification that the web application initialization process is starting.
     * 
     * @param sce the ServletContextEvent containing the ServletContext that is being initialized 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        /*
         * The ServletContext.getRealPath() converts a web content path 
         * (the path in the expanded WAR folder structure on the server's disk file system)
         * to an absolute disk file system path.
         * The "/" represents the web content root.
         */

        sc = sce.getServletContext();
        REALPATH = sc.getRealPath("/");
        System.setProperty("ApplicationPath", REALPATH);
        System.out.println("ApplicationPath set!");

        /*
         * UUID is created during web service utilization on the OpenZoo framework UI
         */
        JSONObject properties = util.getJSONFromFile(REALPATH + "/" + CONFIG_FILE);
        try 
        {        
            // read KV parameters
            parameters.getRedis().setHost(properties.getJSONObject("keyvalue").getString("host"));
            parameters.getRedis().setPort(properties.getJSONObject("keyvalue").getInt("port"));
            
            // read service parameters
            parameters.getGeneral().setComponentID(properties.getJSONObject("service").getString("component_id"));
            parameters.getGeneral().setName(properties.getJSONObject("service").getString("name"));
            parameters.getGeneral().setPath(properties.getJSONObject("service").getString("path"));
            parameters.getGeneral().setDescription(properties.getJSONObject("service").getString("description"));
            parameters.getGeneral().setRealPath(REALPATH);
            
            // set instance id
            parameters.getGeneral().setInstanceID(properties.getString("instance_id"));
        }
        catch (JSONException ex) 
        {
            System.err.println("ERROR retrieving service ID: " + ex);
        }

        //serialize initial parameters to file
        String fbs = REALPATH + "/" + "parameters.ser";
        try 
        {
            SerializationUtil.serialize(parameters, fbs);
        }
        catch (IOException e) 
        {
            e.printStackTrace();
            return;
        }
       
        if (beholder == null) 
        {
            beholder = new OpenZooBeholder();
        }
        
        beholder.setBasicRegistrationParameters(fbs);
        beholder.startIt();
    }

    /*
     *  Receives notification that the ServletContext is about to be shut down.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        beholder.stopIt();
    }
}
