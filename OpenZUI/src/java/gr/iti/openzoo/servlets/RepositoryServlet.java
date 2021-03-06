package gr.iti.openzoo.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gr.iti.openzoo.ui.Blackboard;
import gr.iti.openzoo.ui.Utilities;
import gr.iti.openzoo.pojos.WarFile;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@MultipartConfig
public class RepositoryServlet extends HttpServlet {

    protected static Configuration cfg;
    private Utilities util;
    private static Blackboard kv;
    private JSONObject properties;
    private String localRepository;
    private ArrayList<String> logs = new ArrayList<>();
    
    @Override
    public void init()
    {
        util = (Utilities) getServletContext().getAttribute("util");
        kv = (Blackboard) getServletContext().getAttribute("kv");
        cfg = (Configuration) getServletContext().getAttribute("cfg");
        properties = (JSONObject) getServletContext().getAttribute("properties");
        localRepository = (String) getServletContext().getAttribute("localRepository");
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
        Template repository_tmpl = cfg.getTemplate("repository.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from KV
        
        ArrayList<WarFile> allWarfiles = kv.getWarFiles();
        if (allWarfiles == null)
        {
            err("There was an error in the Key-Value repository, War files list could not be retrieved.");
        }
        else root.put("warfiles", allWarfiles);
        
        root.put("logs", logs);
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter())
        {
            repository_tmpl.process(root, out);
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
                
        switch (action)
        {                
            case "uploadFile":
                
                Part filePart = request.getPart("fileToUpload");
                String fileName = getFileName(filePart);
                
                if (fileName != null && !fileName.isEmpty())
                {
                    File f = new File(localRepository + "/" + fileName);
                    Files.copy(filePart.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Downloaded file at: " + f.getAbsolutePath());
                    inf("WAR file uploaded");
                    JSONObject config = Utilities.readJSONFromWAR(f.getAbsolutePath(), "config.json");
                    
                    String status = "inactive";
                    
                    if (isValid(fileName, config))
                        status = "active";
                    
                    WarFile w = new WarFile(fileName, "1.0", status, config);
                    kv.putWarFile(w);
                }
                break;
                
            case "delete":
                String warcompid = request.getParameter("war-compoid");
                WarFile w = kv.getWarFile(warcompid, true);
                if (w == null)
                    err("There was an error in the Key-Value repository, could not be remove WAR file.");
                else inf("WAR file removed");
                break;
        }
        
        processRequest(request, response);
    }
    
    private boolean isValid(String war, JSONObject config)
    {
        File f = new File(localRepository + "/" + war);
        if (!f.exists())
        {
            System.err.println("War file " + war + " cannot be found in the repository");
            logs.add("War file " + war + " cannot be found in the repository");
            return false;
        }
        
        if (config == null)
        {
            System.err.println("War file " + war + " does not contain a config.json, or config.json is not in json format");
            logs.add("War file " + war + " does not contain a config.json, or config.json is not in json format");
            return false;
        }
        
        String msg;
        
        JSONObject srv = config.optJSONObject("service");
        if (srv == null)
        {
            msg = "service is not specified";
            System.err.println("War file " + war + " has an invalid config.json: " + msg);
            logs.add("War file " + war + " has an invalid config.json: " + msg);
            return false;
        }
        else
        {
            if (srv.optString("component_id").isEmpty())
            {
                msg = "service.component_id is not specified";
                System.err.println("War file " + war + " has an invalid config.json: " + msg);
                logs.add("War file " + war + " has an invalid config.json: " + msg);
                return false;
            }
            
            if (srv.optString("name").isEmpty())
            {
                msg = "service.name is not specified";
                System.err.println("War file " + war + " has an invalid config.json: " + msg);
                logs.add("War file " + war + " has an invalid config.json: " + msg);
                return false;
            }
            
            if (srv.optString("path").isEmpty())
            {
                msg = "service.path is not specified";
                System.err.println("War file " + war + " has an invalid config.json: " + msg);
                logs.add("War file " + war + " has an invalid config.json: " + msg);
                return false;
            }
            
            if (srv.optString("description").isEmpty())
            {
                msg = "service.description is not specified";
                System.err.println("War file " + war + " has an invalid config.json: " + msg);
                logs.add("War file " + war + " has an invalid config.json: " + msg);
                return false;
            }
        }
        
        JSONArray wrks = config.optJSONArray("workers");
        if (wrks == null)
        {
            msg = "workers is not specified";
            System.err.println("War file " + war + " has an invalid config.json: " + msg);
            logs.add("War file " + war + " has an invalid config.json: " + msg);
            return false;
        }
        else
        {
            if (wrks.length() == 0)
            {
                msg = "workers is empty";
                System.err.println("War file " + war + " has an invalid config.json: " + msg);
                logs.add("War file " + war + " has an invalid config.json: " + msg);
                return false;
            }
            else
            {
                for (int i = 0; i < wrks.length(); i++)
                {
                    JSONObject w = wrks.optJSONObject(i);

                    if (w == null)
                    {
                        msg = "workers[" + i + "] is not valid";
                        System.err.println("War file " + war + " has an invalid config.json: " + msg);
                        logs.add("War file " + war + " has an invalid config.json: " + msg);
                        return false;
                    }
                    else
                    {
                        if (w.optString("worker_id").isEmpty())
                        {
                            msg = "workers[" + i + "].worker_id is not specified";
                            System.err.println("War file " + war + " has an invalid config.json: " + msg);
                            logs.add("War file " + war + " has an invalid config.json: " + msg);
                            return false;
                        }
                        
                        JSONArray eps = w.optJSONArray("endpoints");
                        
                        if (eps == null)
                        {
                            msg = "workers[" + i + "].endpoints is not specified";
                            System.err.println("War file " + war + " has an invalid config.json: " + msg);
                            logs.add("War file " + war + " has an invalid config.json: " + msg);
                            return false;
                        }
                        else
                        {
                            if (eps.length() == 0)
                            {
                                msg = "workers[" + i + "].endpoints is empty";
                                System.err.println("War file " + war + " has an invalid config.json: " + msg);
                                logs.add("War file " + war + " has an invalid config.json: " + msg);
                                return false;
                            }
                            else
                            {
                                for (int j = 0; j < eps.length(); j++)
                                {
                                    JSONObject e = eps.optJSONObject(j);
                                    
                                    if (e == null)
                                    {
                                        msg = "workers[" + i + "].endpoints[" + j + "] is not valid";
                                        System.err.println("War file " + war + " has an invalid config.json: " + msg);
                                        logs.add("War file " + war + " has an invalid config.json: " + msg);
                                        return false;
                                    }
                                    else
                                    {
                                        if (e.optString("endpoint_id").isEmpty())
                                        {
                                            msg = "workers[" + i + "].endpoints[" + j + "].endpoint_id is not specified";
                                            System.err.println("War file " + war + " has an invalid config.json: " + msg);
                                            logs.add("War file " + war + " has an invalid config.json: " + msg);
                                            return false;
                                        }
                                        
                                        String type = e.optString("type");
                                        if (!type.equalsIgnoreCase("in") && !type.equalsIgnoreCase("out"))
                                        {
                                            msg = "workers[" + i + "].endpoints[" + j + "].type must be either in or out";
                                            System.err.println("War file " + war + " has an invalid config.json: " + msg);
                                            logs.add("War file " + war + " has an invalid config.json: " + msg);
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }

    // http://stackoverflow.com/questions/2422468/how-to-upload-files-to-server-using-jsp-servlet
    // Case "When you're not on Servlet 3.1 yet, manually get submitted file name"
    private static String getFileName(Part part) 
    {
        for (String cd : part.getHeader("content-disposition").split(";"))
        {
            if (cd.trim().startsWith("filename"))
            {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }
        
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Repository interface";
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
