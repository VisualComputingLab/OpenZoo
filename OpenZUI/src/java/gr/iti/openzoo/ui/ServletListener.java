package gr.iti.openzoo.ui;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Web application lifecycle listener.
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServletListener implements ServletContextListener {

    protected static Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    private Utilities util = new Utilities();
    private static KeyValueCommunication kv;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        try
        {
            String webAppPath = sce.getServletContext().getRealPath("/");
            sce.getServletContext().setAttribute("webAppPath", webAppPath);
            System.out.println("Web app path is " + webAppPath);
            
            JSONObject properties = util.getJSONFromFile(webAppPath + "/config.json");
            System.out.println("Read properties");
            sce.getServletContext().setAttribute("properties", properties);
                        
            try 
            {
                String localRepository = properties.getString("localRepository");
                File fd = new File(localRepository);
                if (!fd.exists())
                {
                    System.out.println("Creating directory " + localRepository);
                    fd.mkdirs();
                }
                sce.getServletContext().setAttribute("localRepository", localRepository);
                System.out.println("Using " + fd.getAbsolutePath() + " as local repository");
                
                kv = new KeyValueCommunication(properties.getJSONObject("keyvalue").getString("host"), properties.getJSONObject("keyvalue").getInt("port"));
                System.out.println("Created KV pool");
                sce.getServletContext().setAttribute("kv", kv);
            }
            catch (JSONException ex) 
            {
                System.err.println("ERROR retrieving keyValue server: " + ex);
            }           
            
            cfg.setDirectoryForTemplateLoading(new File(webAppPath));
            cfg.setDefaultEncoding("UTF-8");
            //cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            System.out.println("Created freemarker config");
            sce.getServletContext().setAttribute("cfg", cfg);
        }
        catch (IOException e)
        {
            System.err.println("IOexception during initializing template configuration: " + e);
        }
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
        kv.stop();
    }
}
