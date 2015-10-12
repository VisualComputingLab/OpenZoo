package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.impl.OpenZooService;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ${Author}
 */
public class ${ServiceID}Impl extends OpenZooService {

    public ${ServiceID}Impl()
    {
        super("${ComponentID}");
        
        log.debug("-- ${ServiceID}Impl()");
    }
    
    public JSONObject get(String action)
    {
        JSONObject response;
        String workerClassName = "gr.iti.openzoo.service.impl.${WorkerID}Worker";
        
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
}
