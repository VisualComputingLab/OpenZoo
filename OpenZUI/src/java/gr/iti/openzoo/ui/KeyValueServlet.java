package gr.iti.openzoo.ui;

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

    private static KeyValueCommunication kv;
    private Utilities util = new Utilities();
    
    @Override
    public void init()
    {
        System.out.println("Calling KeyValueServlet init method");
        
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet KeyValueServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet KeyValueServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {            
            out.close();
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
                        JSONArray jarr = new JSONArray();
                        for (Server srv : allServers)
                            jarr.put(srv.toJSON());
                        json.put("response", jarr);
                    }
                    else
                    {
                        Server server = kv.getServer(name);
                        json.put("response", server.toJSON());
                    }
                    break;

                case "war":
                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        ArrayList<WarFile> allWarfiles = kv.getWarFiles();
                        JSONArray jarr = new JSONArray();
                        for (WarFile war : allWarfiles)
                            jarr.put(war.toJSON());
                        json.put("response", jarr);
                    }
                    else
                    {
                        WarFile war = kv.getWarFile(name);
                        json.put("response", war.toJSON());
                    }
                    break;

                case "topology":
                    if (name == null || name.equalsIgnoreCase("all"))
                    {
                        ArrayList<Topology> allTopologies = kv.getTopologies();
                        JSONArray jarr = new JSONArray();
                        for (Topology topo : allTopologies)
                            jarr.put(topo.toJSON());
                        json.put("response", jarr);
                    }
                    else
                    {
                        Topology topo = kv.getTopology(name);
                        json.put("response", topo.toJSON());
                    }
                    break;

                case "repository":

                    RepositoryParameters repo = kv.getRepositoryParameters();
                    json.put("response", repo.toJSON());
                    break;
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException at doGet: " + e);
        }
        
        response.setContentType("application/json");
        
        try (PrintWriter out = response.getWriter())
        {
            out.println(json);
        }
        
        
        
        //processRequest(request, response);
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
        processRequest(request, response);
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
