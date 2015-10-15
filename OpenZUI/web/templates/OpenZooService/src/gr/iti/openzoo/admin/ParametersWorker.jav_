package gr.iti.openzoo.admin;

import java.util.HashMap;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ParametersWorker {

    private String id = null;
    private HashMap<String, ParametersEndpoint> endpoints = new HashMap<String, ParametersEndpoint>();

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public void addEndpoint(String id, String type)
    {
        ParametersEndpoint ep = new ParametersEndpoint();
        ep.setId(id);
        ep.setType(type);
        endpoints.put(id, ep);
    }
    
    public ParametersEndpoint getEndpoint(String id)
    {
        return endpoints.get(id);
    }
}
