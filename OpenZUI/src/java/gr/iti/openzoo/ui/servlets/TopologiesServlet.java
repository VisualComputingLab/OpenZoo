package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gr.iti.openzoo.ui.Deployer;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.Topology;
import gr.iti.openzoo.ui.Triple;
import gr.iti.openzoo.ui.WarFile;
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
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class TopologiesServlet extends HttpServlet {

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
        
        Template topologies_tmpl = cfg.getTemplate("topologies.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from redis
        ArrayList<Topology> allTopologies = kv.getTopologies();
//        System.out.println("num of topologies in redis: " + allTopologies.size());
        
//        for (Topology tpl : allTopologies)
//        {
//            if (tpl.statusUpdated())
//                kv.putTopology(tpl);
//        }
        root.put("topologies", allTopologies);
        root.put("logs", logs);
        
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
                
//        System.out.println("POST Topologies: action = " + action + ", name = " + name);
        
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
            err("Wrong format for rabbit port number");
            processRequest(request, response);
            return;
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
            err("Wrong format for mongo port number");
            processRequest(request, response);
            return;
        }
        String mongo_user = request.getParameter("topo-mongo-user");
        String mongo_pass = request.getParameter("topo-mongo-pass");
        
        Topology top;
        RequestDispatcher rd;
        
        switch (action)
        {
            case "delete":
                if (name != null)
                {
                    top = kv.getTopology(name, true);
                    
                    if (top == null)
                    {
                        err("There was an error in the Key-Value repository, topology could not be removed");
                    }
                    else
                    {
                        top.deleteTopologyQueues();
                    }
            
                    processRequest(request, response);
                }
                break;
                
            case "create":
                // create topology object
                top = new Topology(name, descr, rabbit_host, rabbit_port, rabbit_user, rabbit_pass, mongo_host, mongo_port, mongo_user, mongo_pass);
                
                // add topology to redis
                kv.putTopology(top);
                
                // At this point we have to open the topology drawing interface
                // This will update the topology and call (GET) the TopologiesServlet servlet again
                rd = request.getRequestDispatcher("DrawTopology");
                //rd.include(request, response);
                rd.forward(request,response);
                break;
                
            case "update":
                // load topology object
                top = kv.getTopology(name);
                
                top.setDescription(descr);
                top.setRabbit_host(rabbit_host);
                top.setRabbit_port(rabbit_port);
                top.setRabbit_user(rabbit_user);
                top.setRabbit_passwd(rabbit_pass);
                top.setMongo_host(mongo_host);
                top.setMongo_port(mongo_port);
                top.setMongo_user(mongo_user);
                top.setMongo_passwd(mongo_pass);
                
                // update topology in redis
                kv.putTopology(top);

                // At this point we have to open the topology drawing interface
                // This will update the topology and call (GET) the TopologiesServlet servlet again
                rd = request.getRequestDispatcher("DrawTopology");
                //rd.include(request, response);
                rd.forward(request,response);
                break;
                
//            case "deploy":
//                Topology topo = kv.getTopology(name);
//                ArrayList<Triple<String, WarFile, JSONObject>> triples = new ArrayList<>();
//                logs.addAll(deployer.produceServerConfiguration(topo, triples));
//                logs.addAll(deployer.deployTopologyServices(topo, triples));
//                //logs.addAll(deployer.deployTopology(name));
//                processRequest(request, response);
//                
//                break;
                
            case "create_config":
                 rd = request.getRequestDispatcher("ConfirmServerConfig");
                 rd.forward(request,response);
                
                break;
                
            case "undeploy":
                logs.addAll(deployer.undeployTopology(name));
                processRequest(request, response);
                break;
                
            case "start":
                logs.addAll(deployer.startTopology(name));
                processRequest(request, response);
                break;
                
            case "stop":
                logs.addAll(deployer.stopTopology(name));
                processRequest(request, response);
                break;
        }
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
