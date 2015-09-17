package gr.iti.openzoo.ui;

import java.util.HashMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class TopologyGraphNode {

    private String component_id;
    private int instances;
    private int workerspercore;
    private HashMap<String, String> requirements = null;

    public TopologyGraphNode(String cid, JSONObject json)
    {
        component_id = cid;
        try
        {
            instances = json.getInt("instances");
            workerspercore = json.getInt("workerspercore");
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in TopologyGraphNode.constr: " + e);
        }
        requirements = new HashMap<>();
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

    public int getWorkerspercore() {
        return workerspercore;
    }

    public void setWorkerspercore(int w) {
        this.workerspercore = w;
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
