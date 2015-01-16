package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooInputConnection;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import gr.iti.openzoo.impl.OpenZooWorker;
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
    public JSONObject doWork(JSONObject json_in) {
        
        log.debug("-- MyTestWorker.doWork");
        
        JSONObject json_out = json_in;
        
        try
        {
            JSONObject whoAmI = new JSONObject();
            whoAmI.put("component_name", serviceParams.getGeneral().getComponentID());
            whoAmI.put("component_instance", serviceParams.getGeneral().getInstanceID());
            whoAmI.put("worker_name", new Throwable().getStackTrace()[0].getClassName().toString());
            whoAmI.put("worker_instance", thread_name);
            
            json_out.put("processedBy", whoAmI);
        }
        catch (JSONException e)
        {
            log.error("JSONException: " + e);
        }
        
        return json_in;
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
        
        log.info("Born!");
        JSONObject jobj_out;
        Message message_in, message_out;
        
        while (!enough) 
        {
            message_in = inFromAny.getNext();
            
            if (message_in == null)
            {
                log.error("Received null message, aborting");
                break;
            }
            else if (message_in.isEmpty())
            {
                log.error("Received empty message, discarding");
                inFromAny.ack(message_in);
                continue;
            }
            
            jobj_out = doWork(message_in.getJSON());
            message_out = new Message(jobj_out);
            
            if (jobj_out.has("all"))
            {
                outToAll.put(message_out);
            }
            else if (jobj_out.has("route"))
            {
                message_out.setRoutingKey("index");
                outToSelected.put(message_out);
            }
            else
            {
                outToAny.put(message_out);
            }
            
            //enough = !inFromAny.ack(message_in);
            inFromAny.ack(message_in);
        }

        log.info("Died!");
    }
}
