package gr.iti.openzoo.ui.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.Server;
import gr.iti.openzoo.ui.Utilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
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
    
    @Override
    public void init()
    {
        System.out.println("Calling Servers init method");
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
        
        Template servers_tmpl = cfg.getTemplate("servers.ftl");
        
        Map<String, Object> root = new HashMap<>();
        
        // Fill data model from redis
        ArrayList<Server> allServers = kv.getServers();
        System.out.println("num of servers in redis: " + allServers.size());
        
        for (Server srv : allServers)
        {
            if (srv.statusUpdated())
                kv.putServer(srv);
        }
        root.put("servers", allServers);
        
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
        String name = request.getParameter("srv-name");
        
        System.out.println("POST Servers: action = " + action + ", name = " + name);
        
        if (action.equalsIgnoreCase("delete") && name != null)
        {
            Server srv = kv.getServer(name, true);
            
            String output = undeployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), "/ServerStatistics");

            System.out.println("---------- UNDEPLOY --------------");
            System.out.println("Undeployment output = " + output);
            System.out.println("---------- UNDEPLOY END --------------");
            
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
        }
        
        String user = request.getParameter("tmc-user");
        String pass = request.getParameter("tmc-pass");
        
        Server srv = new Server(name, address, port, user, pass, "inactive");
        
//        System.out.println("ServersServlet::POST called: " + request);
        
        // add or update new server to redis
        kv.putServer(srv);
        
        if (action.equalsIgnoreCase("create"))
        {
            // deploy ServerStatistics war on new server
            String warfilepath = getServletContext().getRealPath("/") + "ServerStatistics.war";
            String output = deployService("http://" + address + ":" + port, user + ":" + pass, warfilepath, "/ServerStatistics");

            System.out.println("---------- DEPLOY --------------");
            System.out.println("Deployment output = " + output);
            System.out.println("---------- DEPLOY END --------------");
        }
        
        processRequest(request, response);
    }

    private String deployService(String httpserverandport, String servercredentials, String warfilepath, String webservicepath)
    {
        // httpserverandport: server to call, e.g. "http://localhost:8080"
        // servercredentials: server credentials, e.g. "admin:passwd"
        // warfilepath: path to the WAR file
        // webservicepath: service path, e.g. "/SEIndexShardService"
        
        String output = null;
            
        try
        {
            URL url = new URL(httpserverandport + "/manager/text/deploy?path=" + webservicepath + "&update=true");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            //httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
            httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            copyInputStream(new FileInputStream(warfilepath), httpCon.getOutputStream());
            output = convertStreamToString(httpCon.getInputStream());
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during web service deployment: " + e;
        }
        
        return output;
    }
    
    private String undeployService(String httpserverandport, String servercredentials, String webservicepath)
    {
        String output = null;
        
        try
        {
            URL url = new URL(httpserverandport + "/manager/text/undeploy?path=" + webservicepath);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            //httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
            httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during web service undeployment: " + e;
        }
        
        return output;
    }
    
    private static String convertStreamToString(InputStream is) throws IOException {
        //
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        //
        if (is != null) 
        {
            Writer writer = new StringWriter();
 
            char[] buffer = new char[1024];
            try 
            {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) 
                {
                    writer.write(buffer, 0, n);
                }
            } 
            finally 
            {
                is.close();
            }
            
            return writer.toString();
        } 
        else 
        {       
            return "";
        }
    }
    
    private static boolean copyInputStream(InputStream in, OutputStream out)
    {
        byte[] buffer = new byte[1024];
        int len;

        try
        {
            while((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);

            in.close();
            out.close();
        }
        catch(IOException ioe)
        {
            System.err.println("Error while unziping: " + ioe);
            return false;
        }

        return true;
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
}
