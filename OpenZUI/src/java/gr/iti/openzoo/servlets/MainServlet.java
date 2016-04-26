package gr.iti.openzoo.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gr.iti.openzoo.ui.Blackboard;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.ui.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class MainServlet extends HttpServlet {

    protected static Configuration cfg;
    private Utilities util;
    private static Blackboard kv;
    private JSONObject properties;
    
    private ArrayList<String> logs = new ArrayList<>();
    
    @Override
    public void init()
    {
        util = (Utilities) getServletContext().getAttribute("util");
        kv = (Blackboard) getServletContext().getAttribute("kv");
        cfg = (Configuration) getServletContext().getAttribute("cfg");
        properties = (JSONObject) getServletContext().getAttribute("properties");
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

        Template draw_tmpl = cfg.getTemplate("index.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        ArrayList<Topology> allTopos = kv.getTopologies();
        root.put("topologies", allTopos);
        root.put("logs", logs);
                
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter())
        {
            draw_tmpl.process(root, out);
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
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
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
