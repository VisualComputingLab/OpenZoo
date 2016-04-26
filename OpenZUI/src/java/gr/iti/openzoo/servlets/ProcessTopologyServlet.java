package gr.iti.openzoo.servlets;

import freemarker.template.Configuration;
import gr.iti.openzoo.ui.Blackboard;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.ui.Utilities;
import java.io.IOException;
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

    protected static Configuration cfg;
    private Utilities util;
    private static Blackboard kv;
    private JSONObject properties;
    
    @Override
    public void init()
    {
        util = (Utilities) getServletContext().getAttribute("util");
        kv = (Blackboard) getServletContext().getAttribute("kv");
        cfg = (Configuration) getServletContext().getAttribute("cfg");
        properties = (JSONObject) getServletContext().getAttribute("properties");
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try
        {
            request.setCharacterEncoding("UTF-8");
            String name = request.getParameter("topo-name");
            String graphStr = request.getParameter("topo-graph");
            
            System.out.println("ProcessTopologyServlet received: " + name + " " + graphStr);
            
            if (graphStr != null && !graphStr.isEmpty())
            {                
                JSONObject graph_object = new JSONObject(graphStr);
            
                // save graph object to kv
                Topology topo = kv.getTopology(name);
                topo.setGraph_object(graph_object);
                kv.putTopology(topo);
            }
            
            // redirect to Topologies.GET
            response.sendRedirect("Topologies");
        } 
        catch (JSONException ex) 
        {
            System.err.println("JSONException in doPost: " + ex);
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
