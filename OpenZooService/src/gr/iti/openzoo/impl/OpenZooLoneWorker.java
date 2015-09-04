package gr.iti.openzoo.impl;

import gr.iti.openzoo.admin.Message;
import java.util.HashMap;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public abstract class OpenZooLoneWorker extends OpenZooWorker {
        
    private HashMap<String, JSONObject> requests = new HashMap<>();
    
    public OpenZooLoneWorker(String name)
    {
        super(name);
        log.debug("-- OpenZooLoneWorker()");
    }
    
    public abstract String publish(JSONObject obj);
    //public abstract JSONObject get(String hash);
    
    public String putRequest(Message msg)
    {
        String hash = msg.getID();
        requests.put(hash, null);
        return hash;
    }
    
    public JSONObject getResponse(String hash)
    {
        JSONObject response;
        int attempts = 0;
        while (true)
        {
            response = requests.get(hash);
            
            if (response != null || attempts++ >= 60) break;
            
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                log.info("No more sleep");
            }
        }
        
        requests.remove(hash);
        
        return response;
    }
    
    protected void clearRequests()
    {
        requests.clear();
    }
    
    protected HashMap<String, JSONObject> getRequests()
    {
        return requests;
    }
}
