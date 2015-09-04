package gr.iti.openzoo.admin;

import gr.iti.openzoo.util.SerializationUtil;
import gr.iti.openzoo.util.Utilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * Initialization of a non fail safe web service in the node.
 * 
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 * @author Dimitris Samaras <dimitris.samaras@iti.gr>
 */
public class OpenZooBeholder implements Runnable {

    private static Logger log = LogManager.getLogger(OpenZooBeholder.class.getName());
    
    private Utilities util = new Utilities();
    private ServiceParameters parameters = null;
    private Thread runner;
    private int serverPort;
    private String serverHostname;
    private String service_name;
    private Boolean enough = false;
    private KeyValueCommunication kv;

    public OpenZooBeholder() {
        // do nothing
        // caller must also call setBasicRegistrationParameters and startIt if this constructor is used
        
        log.debug("-- OpenZooBeholder()");
    }
    
    @Override
    public void run() {
        log.debug("-- OpenZooBeholder.run");
        
        kv = new KeyValueCommunication(parameters.getRedis().getHost(), parameters.getRedis().getPort());

        while (!enough)
        {
            try
            {
                Thread.sleep(5000);
                if (checkKVForParameterUpdates())
                {
                    log.info("Parameter changes detected in KV, resetting service");
                    // call service reset interface
                    // http://83.212.104.117:8080/OpenZooTemplateService/resources/test?action=reset
                    String getCall = "http://" + serverHostname + ":" + serverPort + "/" + parameters.getGeneral().getComponentID() + parameters.getGeneral().getPath() + "?action=reset";
                    log.debug("Calling: " + getCall);
                    String response = callGET(getCall);
                    log.debug("Response: " + response);
                }
            } 
            catch (InterruptedException ex) 
            {
                log.info("Sleep interrupted: " + ex);
            }
        }
        
        log.debug("-- OpenZooBeholder exits");
    }

    /** 
     * Method setBasicRegistrationParameters
     * 
     * Deserialize and get basic parameters about web service
     * Create web service seperate folder inside the local directory reserved for web services
     * 
     * @param fbs String, the serialized parameters object
     * 
     */ 
    public void setBasicRegistrationParameters(String fbs) {
        log.debug("-- OpenZooBeholder.setBasicRegistrationParameters");

        try
        {
            parameters = (ServiceParameters) SerializationUtil.deserialize(fbs);
        } 
        catch (IOException e) 
        {
            log.error("IOException, error deserializing Service Params " + e);
        } 
        catch (ClassNotFoundException e) 
        {
            log.error("ClassNotFoundException, Service Params " + e);
        }

        service_name = parameters.getGeneral().getName();
    }

    /**
     * Method startIt
     * 
     * Start the initialization of the web service in the Consul
     * 
     */
    public void startIt() {
        log.debug("-- OpenZooBeholder.startIt");
        runner = new Thread(this, "the " + service_name + " beholder thread");
        serverPort = util.getTomcatPort();
        serverHostname = util.getHostname();

        log.info("Starting beholder for service " + service_name + ", tomcat host is " + serverHostname + ", port is " + serverPort);
        runner.start();
    }

    public void stopIt() {
        
        log.debug("-- OpenZooBeholder.stopIt");
        
        enough = true;
        
        runner.interrupt();
    }
    
    private boolean checkKVForParameterUpdatesOld()
    {
        // check if flag component_id:instance_id:reset exists in KV
        // if it exists, delete it and return true
        
        String reset = kv.getValue(parameters.getGeneral().getTopologyID() + ":" + parameters.getGeneral().getComponentID() + ":" + parameters.getGeneral().getInstanceID() + ":reset", true);
        
        if (reset != null && reset.equalsIgnoreCase("true"))
        {
            return true;
        }
        
        return false;
    }
    
    private boolean checkKVForParameterUpdates()
    {
        // check if flag component_id:instance_id:reset exists in KV
        // if it exists, delete it and return true
        
        String reset = kv.getHashValue(parameters.getGeneral().getTopologyID(), parameters.getGeneral().getComponentID() + ":" + parameters.getGeneral().getInstanceID() + ":" + "reset", true);
        
        if (reset != null && reset.equalsIgnoreCase("true"))
        {
            return true;
        }
        
        return false;
    }
    
    private String callGET(String urlstr)
    {
        String output;

        try
        {
            URL url = new URL(urlstr);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            // you need the following if you pass server credentials
            // httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during GET: " + e;
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
}
