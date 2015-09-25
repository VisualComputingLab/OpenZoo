package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import gr.iti.openzoo.ui.Deployer;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.Server;
import gr.iti.openzoo.ui.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
public class ServersServlet extends HttpServlet {

    protected static Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    private Utilities util = new Utilities();
    private static KeyValueCommunication kv;
    private Deployer deployer;
    private ArrayList<String> logs = new ArrayList<>();
    
    @Override
    public void init()
    {
//        System.out.println("Calling Servers init method");
        try
        {
            String webAppPath = getServletContext().getRealPath("/");
//            System.out.println("Web app path is " + webAppPath);
            
            JSONObject properties = util.getJSONFromFile(webAppPath + "/config.json");
            try 
            {        
                kv = new KeyValueCommunication(properties.getJSONObject("keyvalue").getString("host"), properties.getJSONObject("keyvalue").getInt("port"));
            }
            catch (JSONException ex) 
            {
                System.err.println("ERROR retrieving keyValue server: " + ex);
            }           
            
            deployer = new Deployer(properties);
            
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
                
        Template servers_tmpl = cfg.getTemplate("servers.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from redis
        ArrayList<Server> allServers = kv.getServers();
//        System.out.println("num of servers in redis: " + allServers.size());
        
        for (Server srv : allServers)
        {
            if (srv.statusUpdated())
                kv.putServer(srv);
        }
        root.put("servers", allServers);
        root.put("logs", logs);
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter())
        {
            servers_tmpl.process(root, out);
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
        String name = request.getParameter("srv-name");
        
//        System.out.println("POST Servers: action = " + action + ", name = " + name);
        
        if (action.equalsIgnoreCase("delete") && name != null)
        {
            Server srv = kv.getServer(name, true);
            
            if (srv == null)
            {
                err("There was an error in the Key-Value repository, server could not be removed from the cluster");
            }
            else if (srv.isActive())
            {
                JSONObject outjson = deployer.undeployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), "/ServerStatistics");

                if (outjson == null)
                    err("There was an error during undeployment of the statistics service, please check the server logs.");
                else
                {
                    switch (outjson.optString("status")) {
                        case "success":
                            inf("Server was removed from the cluster");
                            break;
                        case "failure":
                            err("Server was removed from the cluster, but there was an error during undeployment of the statistics service");
                            break;
                        default:
                            wrn("Server was removed from the cluster, but undeployment of the statistics service reported: " + outjson);
                    }
                }
            }
            
            processRequest(request, response);
            return;
        }
        
        
        String address = request.getParameter("srv-ip");
        int port = 80;
        try
        {
            port = Integer.parseInt(request.getParameter("tmc-port"));
        }
        catch (NumberFormatException e)
        {
            System.err.println("Wrong format for port number: " + e);
            err("Wrong format for port number");
        }
        
        String user = request.getParameter("tmc-user");
        String pass = request.getParameter("tmc-pass");
        
        Server srv = new Server(name, address, port, user, pass);
        
//        System.out.println("ServersServlet::POST called: " + request);
        
        // add or update new server to redis
        kv.putServer(srv);
        
        if (action.equalsIgnoreCase("create"))
        {
            if (srv.isActive())
            {
                // deploy ServerStatistics war on new server
                String warfilepath = getServletContext().getRealPath("/") + "ServerStatistics.war";
                JSONObject outjson = deployer.deployService("http://" + address + ":" + port, user + ":" + pass, warfilepath, "/ServerStatistics");

                if (outjson == null)
                        err("There was an error during deployment of the statistics service, please check the server logs.");
                else
                {
                    switch (outjson.optString("status")) {
                        case "success":
                            inf("Server was added to the cluster");
                            break;
                        case "failure":
                            err("Server was added to the cluster, but there was an error during deployment of the statistics service");
                            break;
                        default:
                            wrn("Server was added to the cluster, but deployment of the statistics service reported: " + outjson);
                    }
                }
            }
            else
            {
                err("Server was added to the cluster, but it does not seems to respond");
            }
        }
        else if (action.equalsIgnoreCase("update"))
        {
            if (srv.isActive())
            {
                inf("Server parameters were updated");
            }
            else
            {
                err("Server parameters were updated, but the server does not seem to respond");
            }
        }
                
        processRequest(request, response);
    }

    
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servers interface";
    }// </editor-fold>

    private void inf(String s)
    {
        logs.add("INFO:" + s);
    }
    
    private void err(String s)
    {
        logs.add("ERROR:" + s);
    }
    
    private void wrn(String s)
    {
        logs.add("WARN:" + s);
    }
}
