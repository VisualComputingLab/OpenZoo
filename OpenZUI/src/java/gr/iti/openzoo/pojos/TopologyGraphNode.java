package gr.iti.openzoo.pojos;

import java.util.HashMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class TopologyGraphNode {

    private String component_id;
    private int instances = 1;
    private int threadspercore = 1;
    private HashMap<String, String> requirements = null;

    public TopologyGraphNode(String cid, JSONObject json)
    {
        component_id = cid;
        try
        {
            instances = json.getInt("instances");
            threadspercore = json.getInt("threadspercore");
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in TopologyGraphNode.constr: " + e);
        }
        requirements = new HashMap<>();
    }
    
    public TopologyGraphNode(String cid)
    {
        component_id = cid;
        requirements = new HashMap();
    }
    
    public String getName() {
        return component_id;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int inst) {
        this.instances = inst;
    }

    public int getThreadspercore() {
        return threadspercore;
    }

    public void setThreadspercore(int w) {
        this.threadspercore = w;
    }
    
    public void addRequirement(String key, String value)
    {
        getRequirements().put(key, value);
    }

    /**
     * @return the requirements
     */
    public HashMap<String, String> getRequirements() {
        return requirements;
    }
}
