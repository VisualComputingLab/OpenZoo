package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.impl.OpenZooService;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class MyTestModuleImpl extends OpenZooService {
    
    public MyTestModuleImpl()
    {
        super("OpenZooTemplateService");
        
        log.debug("-- MyTestModuleImpl()");                
    }
    
    public JSONObject get(String action)
    {
        JSONObject response;
        String workerClassName = "gr.iti.openzoo.service.impl.MyTestWorker";
        
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
        
    public JSONObject post(JSONObject input)
    {
        try
        {
            input.put("processed", true);
        }
        catch (JSONException e)
        {
            return null;
        }
        
        return input;
    }
}
