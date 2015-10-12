/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.impl.OpenZooService;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author dimitris.samaras
 */
public class InstagramZCrawlerImpl extends OpenZooService {
    
    public InstagramZCrawlerImpl()
    {
        super("InstagramZCrawler");
        
        log.debug("-- InstagramZCrawlImpl()");
    }
    
    
     public JSONObject get(String action)
    {
        JSONObject response;
        String workerClassName = "gr.iti.openzoo.service.impl.InstagramZCrawlerWorker";
        
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
