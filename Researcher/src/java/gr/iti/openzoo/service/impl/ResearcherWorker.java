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
public class ResearcherWorker extends OpenZooWorker {
    
    private final OpenZooInputConnection inConn = new OpenZooInputConnection(this, "input");
    private final OpenZooOutputConnection outConn_1 = new OpenZooOutputConnection(this, "output_1");
    private final OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);

    public ResearcherWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- ResearcherWorker()");
    }

    @Override
    public boolean doWork(Message message) {
        
        throw new UnsupportedOperationException("Not Used.");
    }

    @Override
    public String publish(JSONObject obj) {
 
        Message message = createEmptyMessage();
               
        // put the whole input json or a part of it in the payload
        // send it to some output endpoint
        // and put it to the request map
        try
        {
            message.getHeader().put("search", true);
        }
        catch (JSONException e)
        {
            System.err.println("Could not set search header: " + e);
            return null;
            
        }
        message.setPayload(obj);
        message.setSuccess(true);

        outConn_1.put(message);

        return putRequest(message);
    }

    @Override
    public void run()
    {
        log.debug("-- ResearcherWorker.run");
        
        if (!logConn.init())
        {
            log.error("Error by endpoint initialization");
            return;
        }
        
        logConn.debug("Running...");

        if (!inConn.init())
        {
            log.error("Error by input endpoint initialization");
            logConn.error("Error by input endpoint initialization");
            //return;
        }

        if (!outConn_1.init())
        {
            log.error("Error by output_1 endpoint initialization");
            logConn.error("Error by output_1 endpoint initialization");
            //return;
        }

        

        // Do your initializing here
        
        log.info("Born!");
        logConn.info("Born!");
        Message message;
        
        while (!enough) 
        {
            // get next message from queue
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

            
            // acknowledge message and put results into request map
            inConn.ack(message);
            requests.put(message.getID(), message.getPayload());
        }
        
        // Do your cleaning here
        requests.clear();
        log.info("Died!");
        logConn.info("Died!");
    }
}
