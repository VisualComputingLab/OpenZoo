package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooInputConnection;
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import gr.iti.openzoo.impl.OpenZooWorker;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class DownloadWorker extends OpenZooWorker {

    private OpenZooInputConnection inConn = new OpenZooInputConnection(this, "input");
    private OpenZooOutputConnection outConn = new OpenZooOutputConnection(this, "output_1");
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
    
    public DownloadWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- DownloadWorker()");
    }
    
    @Override
    public boolean doWork(Message message) {
        
        // do nothing, just forward the message
        
        JSONObject pld = message.getPayload();
        
        try
        {
            pld.put("downloaded", true);
            message.setPayload(pld);
        }
        catch (JSONException e)
        {
            log.error("JSONException: " + e);
            logConn.error("JSONException: " + e);
            return false;
        }
        
        return true;
    }

    @Override
    public void run()
    {
        log.debug("-- DownloadWorker.run");
        
        if (!logConn.init())
        {
            log.error("Error by endpoint initialization");
            return;
        }
        
        logConn.debug("Running...");
        
        if (!inConn.init() || !outConn.init())
        {
            log.error("Error by endpoint initialization");
            logConn.error("Error by endpoint initialization");
            return;
        }
        
        // Do your initializing here
        
        log.info("Born!");
        Message message;
        
        while (!enough) 
        {
            message = inConn.getNext();
            
            if (message == null)
            {
                log.error("Received null message, aborting");
                logConn.error("Received null message, aborting");
                break;
            }
            else if (message.isEmpty())
            {
                log.error("Received empty message, discarding");
                logConn.error("Received empty message, discarding");
                inConn.ack(message);
                continue;
            }
            
            JSONObject pld = message.getPayload();
            
            // Do your processing here
            boolean success = doWork(message);
            
            if (success)
            {
                message.setSuccess(success);
                
                // send results to next component
                outConn.put(message);
            }
            
            // acknowledge incomming message
            inConn.ack(message);
        }
        
        // Do your cleaning here

        log.info("Died!");
        logConn.info("Died!");
    }

    @Override
    public String publish(JSONObject obj) {
        throw new UnsupportedOperationException("Not Used.");
    }

}
