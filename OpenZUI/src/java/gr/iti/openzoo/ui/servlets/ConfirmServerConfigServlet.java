package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gr.iti.openzoo.ui.Deployer;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.pojos.Triple;
import gr.iti.openzoo.pojos.WarFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ConfirmServerConfigServlet extends HttpServlet {
    
    protected static Configuration cfg;
    private static KeyValueCommunication kv;
    private JSONObject properties;
    private Deployer deployer;
    private ArrayList<String> logs = new ArrayList<>();

    @Override
    public void init()
    {
        kv = (KeyValueCommunication) getServletContext().getAttribute("kv");
        cfg = (Configuration) getServletContext().getAttribute("cfg");
        properties = (JSONObject) getServletContext().getAttribute("properties");
        
        deployer = new Deployer(properties, kv);
    }  
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String name = request.getParameter("topo-name");
        Topology topo = kv.getTopology(name);
        ArrayList<JSONObject> triples = new ArrayList<>();
        logs.addAll(deployer.produceServerConfiguration(topo, triples));
        
        // split instances to server buckets
        HashMap<String, ArrayList<JSONObject>> server2instances = new HashMap<>();
        String server_id;
        ArrayList<JSONObject> instances;
        
        for (JSONObject triple : triples)
        {
            server_id = triple.optString("server_id");
            instances = server2instances.get(server_id);
            if (instances == null)
            {
                instances = new ArrayList<>();
                server2instances.put(server_id, instances);
            }
            instances.add(triple);
        }
        
        // build page using the triples
        Template confirm_config = cfg.getTemplate("confirm_config.ftl");
        
        Map<String, Object> root = new HashMap<>();
                
        root.put("topology_name", name);
        root.put("server2instances", server2instances);
        root.put("logs", logs);
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter())
        {
            confirm_config.process(root, out);
        }
        catch (TemplateException ex)
        {
            System.err.println("TemplateException during processing template: " + ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logs.clear();
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logs.clear();
        String action = request.getParameter("action");
        String name = request.getParameter("topo-name");
        Topology topo = kv.getTopology(name);
        ArrayList<Triple<String, WarFile, JSONObject>> triples;
        
        if (action.equalsIgnoreCase("create_config")) //comes from TopologiesServlet.deploy
        {
            processRequest(request, response);
        }
        else if (action.equalsIgnoreCase("deploy_services"))// comes from self.submit
        {
            String s_configJson = request.getParameter("topo-config");
            if (s_configJson != null && !s_configJson.isEmpty())
            {
                JSONArray configJson;
                try
                {
                    configJson = new JSONArray(s_configJson);
                    logs.addAll(deployer.deployTopologyServices(topo, configJson));
                }
                catch (JSONException e)
                {
                    System.err.println("JSONException while parsing configuration: " + e);
                    logs.add("ERROR:" + "JSONException while parsing configuration: " + e);
                }
            }
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("Location", "Topologies");       
        }        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Server config confirmation";
    }// </editor-fold>
}
