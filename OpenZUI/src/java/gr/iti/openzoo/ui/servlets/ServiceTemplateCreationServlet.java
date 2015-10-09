package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.Utilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServiceTemplateCreationServlet extends HttpServlet {

    protected static Configuration cfg;
    private Utilities util;
    private static KeyValueCommunication kv;
    private JSONObject properties;
    private ArrayList<String> logs = new ArrayList<>();
    private static String[] dirs = {
        "nbproject/",
        "web/META-INF/", 
        "web/WEB-INF/", 
        "src/conf/", 
        "src/java/gr/iti/openzoo/service/rest/", 
        "src/java/gr/iti/openzoo/service/impl/"
    };
    private static String[] files = {
        "build.xml",
        "nbproject/ant-deploy.xml",
        "nbproject/genfiles.properties",
        "nbproject/build-impl.xml", 
        "nbproject/project.properties", 
        "nbproject/project.xml", 
        "nbproject/rest-build.xml", 
        "web/index.jsp", 
        "web/config.json", 
        "web/META-INF/context.xml", 
        "web/WEB-INF/web.xml", 
        "src/conf/MANIFEST.MF", 
        "src/java/gr/iti/openzoo/service/rest/${ServiceID}.java", 
        "src/java/gr/iti/openzoo/service/impl/${ComponentID}CrossOriginResourceSharingFilter.java", 
        "src/java/gr/iti/openzoo/service/impl/${ComponentID}ServletListener.java", 
        "src/java/gr/iti/openzoo/service/impl/${ServiceID}Impl.java", 
        "src/java/gr/iti/openzoo/service/impl/${WorkerID}Worker.java"
    };
    
    @Override
    public void init()
    {
        util = (Utilities) getServletContext().getAttribute("util");
        kv = (KeyValueCommunication) getServletContext().getAttribute("kv");
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
        
        Template templates_tmpl = cfg.getTemplate("templates.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from redis
        root.put("logs", logs);
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter())
        {
            templates_tmpl.process(root, out);
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
        
        logs.clear();
        
        System.out.println("request is: " + request);
        
        String proglang = request.getParameter("tmpl-proglang");
        
        if (proglang != null)
            switch (proglang)
            {
                case "Java":
                    createJavaService(request, response);
                    break;
                case "C++":
                    err("C++ is not supported yet");
                    break;
                case "Python":
                    err("Python is not supported yet");
                    break;
            }
            
        
        processRequest(request, response);
    }

    private void createJavaService(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String author = request.getParameter("tmpl-author");
        String componentID = request.getParameter("tmpl-componentID");
        String serviceID = request.getParameter("tmpl-serviceID");
        String workerID = request.getParameter("tmpl-workerID");
        String resourcePath = request.getParameter("tmpl-resourcePath");
        String description = request.getParameter("tmpl-description");

        boolean hasInput = request.getParameter("tmpl-hasInput") != null;
        int numOutputs = Integer.parseInt(request.getParameter("tmpl-numOutputs"));
        boolean queueLogging = request.getParameter("tmpl-queueLogging") != null;
        boolean usesMongo = request.getParameter("tmpl-usesMongo") != null;
        HashSet<String> requiredParameters = null;
        String s_req = request.getParameter("tmpl-requiredParameters");
        if (s_req != null && !s_req.trim().isEmpty())
        {
            String [] split = s_req.split(",");
            String trimmed;
            requiredParameters = new HashSet<>();
            for (int i = 0; i < split.length; i++)
            {
                trimmed = split[i].trim();
                if (!trimmed.isEmpty())
                    requiredParameters.add(trimmed);
            }
        }

        Map<String, Object> root = new HashMap<>();

        root.put("Author", author);
        root.put("ComponentID", componentID);
        root.put("ServiceID", serviceID);
        root.put("WorkerID", workerID);
        root.put("ResourcePath", resourcePath);
        root.put("Description", description);
        if (hasInput)
            root.put("HasInput", hasInput);
        root.put("NumOutputs", numOutputs);
        if (queueLogging)
            root.put("QueueLogging", queueLogging);
        if (usesMongo)
            root.put("UsesMongo", usesMongo);
        if (requiredParameters != null && !requiredParameters.isEmpty())
            root.put("RequiredParameters", requiredParameters);


        String filename;
        Template tmpl;
        File dir;
        String templDir = "templates/java";
        String baseDir = "templates/tmp/" + componentID;

        dir = new File(baseDir);
        if (dir.exists())
            FileUtils.deleteQuietly(dir);
        dir.mkdirs();

        for (int i = 0; i < dirs.length; i++)
        {
            dir = new File(baseDir + "/" + dirs[i]);
            dir.mkdirs();
        }

        for (int i = 0; i < files.length; i++)
        {
//            filename = baseDir + "/" + files[i];
            filename = files[i];

            filename = filename.replace("_COMPONENTID_", componentID);
            filename = filename.replace("_SERVICEID_", serviceID);
            filename = filename.replace("_WORKERID_", workerID);

            tmpl = cfg.getTemplate(templDir + "/" + filename + ".ftl");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(baseDir + "/" + filename)))
            {
                tmpl.process(root, writer);
            }
            catch (TemplateException ex)
            {
                System.err.println("TemplateException during creating template for file " + filename + ": " + ex);
                err("TemplateException during creating template for file " + filename + ": " + ex);
            }
        }

        if (Utilities.compressDirectory(baseDir))
        {
            String s_ZipFile = baseDir + ".zip";
            File downloadFile = new File(s_ZipFile);
            ServletContext context = request.getServletContext();
            String mimeType = context.getMimeType(s_ZipFile);
            if (mimeType == null)
            {        
                // set to binary type if MIME mapping not found
                mimeType = "application/octet-stream";
            }
            System.out.println("MIME type: " + mimeType);
            response.setContentType(mimeType);
            response.setContentLength((int) downloadFile.length());

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
            response.setHeader(headerKey, headerValue);
            byte[] buffer = new byte[4096];
            int bytesRead;
            try (FileInputStream inStream = new FileInputStream(downloadFile); OutputStream outStream = response.getOutputStream())
            {
                while ((bytesRead = inStream.read(buffer)) != -1)
                {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
        }
        else
        {
            System.err.println("Directory could not be compressed");
            err("Directory could not be compressed");
        }
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Service Template Creator";
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
