package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.impl.OpenZooService;
import org.codehaus.jettison.json.JSONObject;
<#if IsBroker??>
import gr.iti.openzoo.impl.OpenZooWorker;
</#if>

/**
 *
 * @author ${Author}
 */
public class ${ComponentID}Service extends OpenZooService {

    public ${ComponentID}Service()
    {
        super("${ComponentID}");
        
        log.debug("-- ${ComponentID}Service()");
    }
    
    public JSONObject get(String action)
    {
        JSONObject response;
        String workerClassName = "gr.iti.openzoo.service.impl.${ComponentID}Worker";
        
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

<#if IsBroker??>
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
</#if>}