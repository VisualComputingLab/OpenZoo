package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooInputConnection;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import gr.iti.openzoo.impl.OpenZooWorker;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class MyTestWorker extends OpenZooWorker {

    private OpenZooInputConnection inFromAny = new OpenZooInputConnection(this, "ep_from");
    private OpenZooOutputConnection outToAny = new OpenZooOutputConnection(this, "ep_to_1");
    private OpenZooOutputConnection outToAll = new OpenZooOutputConnection(this, "ep_to_2");
    private OpenZooOutputConnection outToSelected = new OpenZooOutputConnection(this, "ep_to_3");
    
    public MyTestWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- MyTestWorker()");
    }
    
    @Override
    public boolean doWork(Message message) {
        
        log.debug("-- MyTestWorker.doWork");
                
        try
        {   
            // either create and insert a new payload:
            //JSONObject json_out = new JSONObject();
            //json_out.put("processed", true);
            //message.setPayload(json_out);
            
            // or work on the existing one:
            message.getPayload().put("processed", true);
        }
        catch (JSONException e)
        {
            log.error("JSONException: " + e);
            return false;
        }
        
        return true;
    }

    @Override
    public void run()
    {
        log.debug("-- MyTestWorker.doWork");
        
        if (!inFromAny.init() || !outToAll.init() || !outToSelected.init() || !outToAny.init())
        {
            log.error("Error by endpoint initialization");
            return;
        }
        
        // Access hier the required parameters
        String conKey = getRequiredParameter("consumerKey");
        String conSec = getRequiredParameter("consumerSecret");
        String accTok = getRequiredParameter("accessToken");
        String accTokSec = getRequiredParameter("accessTokenSecret");
        JSONArray keywords = null;
        try
        {
            keywords = new JSONArray(getRequiredParameter("keywords"));
        }
        catch (JSONException e)
        {
            log.error("JSONException while reading keywords from KV: " + e);
            return;
        }
        
        log.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + keywords);
        
        log.info("Born!");
        Message message;
        
        while (!enough) 
        {
            message = inFromAny.getNext();
            
            if (message == null)
            {
                log.error("Received null message, aborting");
                break;
            }
            else if (message.isEmpty())
            {
                log.error("Received empty message, discarding");
                inFromAny.ack(message);
                continue;
            }
            
            boolean success = doWork(message);
            message.setSuccess(success);
            
            
            String type = message.getPayload().optString("type", "text");
            
            if (type.equalsIgnoreCase("text"))
            {
                outToAny.put(message);
            }
            else if (type.equalsIgnoreCase("image"))
            {
                message.setRoutingKey("index");
                outToSelected.put(message);
            }
            else
            {
                outToAll.put(message);
            }
            
            inFromAny.ack(message);
        }

        log.info("Died!");
    }
}
