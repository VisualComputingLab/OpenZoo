package gr.iti.openzoo.servlets;

import gr.iti.openzoo.ui.Blackboard;
import gr.iti.openzoo.ui.MessageStatistics;
import gr.iti.openzoo.pojos.Server;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.ui.Utilities;
import gr.iti.openzoo.pojos.WarFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
public class KeyValueServlet extends HttpServlet {

    private Utilities util;
    private static Blackboard kv;
    private JSONObject properties;
    
    @Override
    public void init()
    {
        util = (Utilities) getServletContext().getAttribute("util");
        kv = (Blackboard) getServletContext().getAttribute("kv");
        properties = (JSONObject) getServletContext().getAttribute("properties");
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
        
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        
        JSONObject json = new JSONObject();
        
        try
        {
            json.put("action", action);
            json.put("name", name);
            
            switch (action)
            {
                case "server":
                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        ArrayList<Server> allServers = kv.getServers();
                        if (allServers != null)
                        {
                            JSONArray jarr = new JSONArray();
                            for (Server srv : allServers)
                                jarr.put(srv.toJSON());
                            json.put("response", jarr);
                        }
                    }
                    else
                    {
                        Server server = kv.getServer(name);
                        if (server != null)
                            json.put("response", server.toJSON());
                    }
                    break;

                case "war":
                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        ArrayList<WarFile> allWarfiles = kv.getWarFiles();
                        if (allWarfiles != null)
                        {
                            JSONArray jarr = new JSONArray();
                            for (WarFile war : allWarfiles)
                                jarr.put(war.toJSON());
                            json.put("response", jarr);
                        }
                    }
                    else
                    {
                        WarFile war = kv.getWarFile(name);
                        if (war != null)
                            json.put("response", war.toJSON());
                    }
                    break;

                case "topology":
                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        ArrayList<Topology> allTopologies = kv.getTopologies();
                        if (allTopologies != null)
                        {
                            JSONArray jarr = new JSONArray();
                            for (Topology topo : allTopologies)
                                jarr.put(topo.toJSON());
                            json.put("response", jarr);
                        }
                    }
                    else
                    {
                        Topology topo = kv.getTopology(name);
                        if (topo != null)
                            json.put("response", topo.toJSON());
                    }
                    break;
                    
                case "topoconf":

                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        ArrayList<Topology> allTopologies = kv.getTopologies();
                        if (allTopologies != null)
                        {
                            JSONArray jarr = new JSONArray();
                            for (Topology topo : allTopologies)
                                jarr.put(topo.getServerStatus());
                            json.put("response", jarr);
                        }
                    }
                    else
                    {
                        Topology topo = kv.getTopology(name);
                        if (topo != null)
                            json.put("response", topo.getServerStatus());
                    }
                    break;
                    
                case "endpointstats":
                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        json.put("response", "Please specify a topology");
                    }
                    else
                    {
                        Topology topo = kv.getTopology(name);
                        if (topo != null)
                        {
                            //HashMap<String, ArrayList<Long>> eps = kv.getEndpointStatValues(name);
                            MessageStatistics eps = kv.getEndpointStats(name);
                            if (eps != null)
                                json.put("response", eps.getJSON());
                        }
                    }
                    break;
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException at doGet: " + e);
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        
        try (PrintWriter out = response.getWriter())
        {
            out.println(json);
        }
        
        
        //processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "KeyValue Servlet";
    }// </editor-fold>
}
