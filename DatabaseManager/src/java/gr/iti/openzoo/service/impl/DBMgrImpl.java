package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.impl.OpenZooService;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class DBMgrImpl extends OpenZooService {

    public DBMgrImpl()
    {
        super("DatabaseManager");
        
        log.debug("-- DBMgrImpl()");
    }
    
    public JSONObject get(String action)
    {
        JSONObject response;
        String workerClassName = "gr.iti.openzoo.service.impl.DBWorker";
        
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
