package gr.iti.openzoo.ui;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
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
public class Topologies extends HttpServlet {

    protected static Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    private Utilities util = new Utilities();
    private static KeyValueCommunication kv;
    
    @Override
    public void init()
    {
        System.out.println("Calling Topologies init method");
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
        
        Template topologies_tmpl = cfg.getTemplate("topologies.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from redis
        ArrayList<Topology> allTopologies = kv.getTopologies();
        System.out.println("num of topologies in redis: " + allTopologies.size());
        
//        for (Topology tpl : allTopologies)
//        {
//            if (tpl.statusUpdated())
//                kv.putTopology(tpl);
//        }
        root.put("topologies", allTopologies);
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter())
        {
            topologies_tmpl.process(root, out);
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
        
        String action = request.getParameter("action");        
        String name = request.getParameter("topo-name");
        
        System.out.println("POST Topologies: action = " + action + ", name = " + name);
        
        if (action.equalsIgnoreCase("delete") && name != null)
        {
            kv.getTopology(name, true);
            
            processRequest(request, response);
            return;
        }
        
        
        String descr = request.getParameter("topo-descr");
        
        String rabbit_host = request.getParameter("topo-rabbit-host");
        int rabbit_port = 5672;
        try
        {
            rabbit_port = Integer.parseInt(request.getParameter("topo-rabbit-port"));
        }
        catch (NumberFormatException e)
        {
            System.err.println("Wrong format for port number: " + e);
        }
        String rabbit_user = request.getParameter("topo-rabbit-user");
        String rabbit_pass = request.getParameter("topo-rabbit-pass");
        
        String mongo_host = request.getParameter("topo-mongo-host");
        
        int mongo_port = 27017;
        try
        {
            mongo_port = Integer.parseInt(request.getParameter("topo-mongo-port"));
        }
        catch (NumberFormatException e)
        {
            System.err.println("Wrong format for port number: " + e);
        }
        String mongo_user = request.getParameter("topo-mongo-user");
        String mongo_pass = request.getParameter("topo-mongo-pass");
        
        
        Topology top = new Topology(name, descr, rabbit_host, rabbit_port, rabbit_user, rabbit_pass, mongo_host, mongo_port, mongo_user, mongo_pass);
        
        System.out.println("Topologies::POST called: " + request);
        
        // add or update new server to redis
        kv.putTopology(top);
        
        
        // At this point we have to open the topology drawing interface
        // This will update the topology and call (GET) the Topologies servlet again
        RequestDispatcher rd = request.getRequestDispatcher("DrawTopology");
        //rd.include(request, response);
        rd.forward(request,response);
        
        //processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Topologies interface";
    }// </editor-fold>
}