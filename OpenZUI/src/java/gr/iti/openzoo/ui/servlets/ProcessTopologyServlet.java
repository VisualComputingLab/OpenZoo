package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.Topology;
import gr.iti.openzoo.ui.Utilities;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ProcessTopologyServlet extends HttpServlet {

    protected static Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    private Utilities util = new Utilities();
    private static KeyValueCommunication kv;
    
    @Override
    public void init()
    {
        System.out.println("Calling ProcessTopology init method");
        try
        {
            String webAppPath = getServletContext().getRealPath("/");
            System.out.println("Web app path is " + webAppPath);
            
            JSONObject properties = util.getJSONFromFile(webAppPath + "/config.json");
            try 
            {        
                kv = new KeyValueCommunication(properties.getJSONObject("keyvalue").getString("host"), properties.getJSONObject("keyvalue").getInt("port"));
            }
            catch (JSONException ex) 
            {
                System.err.println("ERROR retrieving keyValue server: " + ex);
            }           
            
            cfg.setDirectoryForTemplateLoading(new File(webAppPath));
            cfg.setDefaultEncoding("UTF-8");
            //cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        }
        catch (IOException e)
        {
            System.err.println("IOexception during initializing template configuration: " + e);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try
        {
            String name = request.getParameter("topo-name");
            String graphStr = request.getParameter("topo-graph");
            
            System.out.println("ProcessTopologyServlet received: " + name + " " + graphStr);
            
            if (graphStr != null && !graphStr.isEmpty())
            {
                JSONObject graph = new JSONObject(graphStr);
            
                // save graph object to kv
                Topology topo = kv.getTopology(name);
                topo.setGraph_object(graph);
                kv.putTopology(topo);
            }
            
            // redirect to Topologies.GET
            response.sendRedirect("Topologies");
        } 
        catch (JSONException ex) 
        {
            Logger.getLogger(ProcessTopologyServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Process Topology interface";
    }// </editor-fold>
}
