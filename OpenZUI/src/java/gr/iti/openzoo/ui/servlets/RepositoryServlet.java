package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.RepositoryParameters;
import gr.iti.openzoo.ui.Utilities;
import gr.iti.openzoo.ui.WarFile;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@MultipartConfig
public class RepositoryServlet extends HttpServlet {

    protected static Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    private Utilities util = new Utilities();
    private static KeyValueCommunication kv;
    private static String localRepository = null;
    private ArrayList<String> logs = new ArrayList<>();
    
    @Override
    public void init()
    {
//        System.out.println("Calling Repository init method");
        try
        {
            String webAppPath = getServletContext().getRealPath("/");
//            System.out.println("Web app path is " + webAppPath);
            
            JSONObject properties = util.getJSONFromFile(webAppPath + "/config.json");
            try 
            {        
                kv = new KeyValueCommunication(properties.getJSONObject("keyvalue").getString("host"), properties.getJSONObject("keyvalue").getInt("port"));
                localRepository = properties.getString("localRepository");
                File fd = new File(localRepository);
                if (!fd.exists()) fd.mkdir();
                System.out.println("Using " + fd.getAbsolutePath() + " as local repository");
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
        Template repository_tmpl = cfg.getTemplate("repository.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from redis
        RepositoryParameters repo = kv.getRepositoryParameters();
        if (repo == null)
        {
            err("There was an error in the Key-Value repository, Repository Parameters could not be retrieved.");
        }
        else root.put("ftp", repo);
        
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
        
        //System.out.println(request);
        
        String action = request.getParameter("action");
        
//        System.out.println("POST Repository: action = " + action);
        
        switch (action)
        {
            case "updateRepo":
//                System.out.println("Updating repo params");
                String host = request.getParameter("ftp-host");
                int port = 21;
                try
                {
                    port = Integer.parseInt(request.getParameter("ftp-port"));
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Wrong format for port number: " + e);
                    err("Wrong format for port number");
                }

                String user = request.getParameter("ftp-user");
                String pass = request.getParameter("ftp-pass");
                String path = request.getParameter("ftp-path");

                RepositoryParameters repo = new RepositoryParameters(host, port, user, pass, path);

                //System.out.println("RepositoryServlet::POST called: " + request);

                // add or update new server to redis
                kv.putRepositoryParameters(repo);
                break;
                
            case "uploadFile":
//                System.out.println("Uploading war file");
                
                Part filePart = request.getPart("fileToUpload");
                String fileName = getFileName(filePart);
                
                if (fileName != null && !fileName.isEmpty())
                {
                    File f = new File(localRepository + "/" + fileName);
                    Files.copy(filePart.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Downloaded file at: " + f.getAbsolutePath());
                    inf("WAR file uploaded");
                    JSONObject config = Utilities.readJSONFromWAR(f.getAbsolutePath(), "config.json");
                    
                    if (config == null)
                    {
                        System.err.println("War file does not contain a config.json, or config.json is not in json format");
                        err("War file does not contain a config.json, or config.json is not in json format");
                    }
                    else
                    {
                        WarFile w = new WarFile(fileName, localRepository, "1.0", "inactive", config);
                        kv.putWarFile(w);
                    }
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
