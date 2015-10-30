package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.impl.OpenZooService;
import org.codehaus.jettison.json.JSONObject;
import gr.iti.openzoo.impl.OpenZooWorker;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class SearcherImpl extends OpenZooService {

    public SearcherImpl()
    {
        super("SearchAgent");
        
        log.debug("-- SearcherImpl()");
    }
    
    public JSONObject get(String action)
    {
        JSONObject response;
        String workerClassName = "gr.iti.openzoo.service.impl.SearchWorker";
        
        switch (action) {
            case "start":
                response = startWorkers(workerClassName);
                break;
            case "stop":
                response = stopWorkers();
                break;
            case "reset":
                response = reset();
                break;
            case "status":
                response = status();
                break;
            default:
                response = null;
                break;
        }
        
        return response;
    }

    public JSONObject post(JSONObject content)
    {
        OpenZooWorker ozw = null;
        
        if (workerUnion.size() > 0)
            ozw = (OpenZooWorker) workerUnion.get(0);
        
        if (ozw == null) return null;
        
        String ticket = ozw.publish(content);
        int TIMEOUT_IN_SECS = 10;
        
        if (ticket != null)
            return ozw.getResponse(ticket, TIMEOUT_IN_SECS);
        
        return null;
    }
}
