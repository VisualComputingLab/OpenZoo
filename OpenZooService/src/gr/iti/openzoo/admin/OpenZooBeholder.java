package gr.iti.openzoo.admin;

import gr.iti.openzoo.util.SerializationUtil;
import gr.iti.openzoo.util.Utilities;
import java.io.IOException;
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
                    String getCall = "http://" + serverHostname + ":" + serverPort + "/" + parameters.getGeneral().getComponentID() + parameters.getGeneral().getPath() + "?action=reset";
                    log.debug("Calling: " + getCall);
                    String response = util.callGET(new URL(getCall), null, null);
                    log.debug("Response: " + response);
                }
            } 
            catch (InterruptedException ex) 
            {
                log.info("Sleep interrupted: " + ex);
            }
            catch (IOException ex) 
            {
                log.info("IOException while beholder calls reset: " + ex);
            }
        }
        
        log.debug("-- OpenZooBeholder exits");
    }

    /** 
     * Method setBasicRegistrationParameters
     * 
     * Deserialize and get basic parameters about web service
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
     * Start the initialization of the web service
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
        
    private boolean checkKVForParameterUpdates()
    {
        // check if flag component_id:instance_id:reset exists in KV
        // if it exists, delete it and return true
        
        String reset = kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "reset:" + parameters.getGeneral().getComponentID() + ":" + parameters.getGeneral().getInstanceID() + ":" + "reset", true);
        
        if (reset != null && reset.equalsIgnoreCase("true"))
        {
            return true;
        }
        
        return false;
    }
}
