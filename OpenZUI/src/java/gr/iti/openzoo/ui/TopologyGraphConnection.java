package gr.iti.openzoo.ui;

import java.util.HashSet;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class TopologyGraphConnection {

    private String source_component = null;
    private String target_component = null;
    private String source_worker = null;
    private String target_worker = null;
    private String source_endpoint = null;
    private String target_endpoint = null;
    private int target_instance = -1;
    private int mapping = 0;
    private String queue_name = null;
    private String exchange_name = null;
    private HashSet<String> routing_keys = null;
    
    public TopologyGraphConnection(String s_c, String s_w, String s_e, String t_c, String t_w, String t_e, int t_i, JSONObject json)
    {
        source_component = s_c;
        target_component = t_c;
        source_worker = s_w;
        target_worker = t_w;
        source_endpoint = s_e;
        target_endpoint = t_e;
        target_instance = t_i;
        
        try
        {
            mapping = json.getInt("mapping");
            queue_name = json.optString("queue_name", null);
            exchange_name = json.optString("exchange_name", null);
            JSONArray rkeys = json.optJSONArray("routing_keys");
            if (rkeys != null)
            {
                routing_keys = new HashSet<>();
                for (int i = 0; i < rkeys.length(); i++)
                {
                    if (!rkeys.isNull(i))
                        routing_keys.add(rkeys.getString(i));
                }
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in TopologyGraphConnection.constr: " + e);
            System.err.println("json is: " + json.toString());
        }
    }
    
    public TopologyGraphConnection(String topo_name, String transition_id, String s_c, String s_w, String s_e, String t_c, String t_w, String t_e, int t_i, String mapstr, String routkeys)
    {
        source_component = s_c;
        target_component = t_c;
        source_worker = s_w;
        target_worker = t_w;
        source_endpoint = s_e;
        target_endpoint = t_e;
        target_instance = t_i;
        
        switch (mapstr)
        {
            case "conn_available":
                mapping = 0;
//                queue_name = topo_name + "_" + source_component + "_" + target_component + "_" + transition_id;
                queue_name = topo_name + "_" + target_component + "_" + target_worker + "_" + target_endpoint;
                break;
                
            case "conn_all":
                mapping = 1;
//                exchange_name = topo_name + "_" + source_component + "_" + target_component + "_" + transition_id;
                exchange_name = topo_name + "_" + target_component + "_" + target_worker + "_" + target_endpoint;
                break;
                
            case "conn_route":
                mapping = 2;
//                exchange_name = topo_name + "_" + source_component + "_" + target_component + "_" + transition_id;
                exchange_name = topo_name + "_" + target_component + "_" + target_worker + "_" + target_endpoint;
                routing_keys = new HashSet<>();
                String [] split = routkeys.split(",");
                for (int k = 0; k < split.length; k++)
                {
                    routing_keys.add(split[k].trim());
                }
                break;
        }
    }
    
    public String getSource_component() {
        return source_component;
    }

    public void setSource_component(String source_component) {
        this.source_component = source_component;
    }
    
    public String getTarget_component() {
        return target_component;
    }

    public void setTarget_component(String target_component) {
        this.target_component = target_component;
    }
    
    /**
     * @return the source_worker
     */
    public String getSource_worker() {
        return source_worker;
    }

    /**
     * @param source_worker the source_worker to set
     */
    public void setSource_worker(String source_worker) {
        this.source_worker = source_worker;
    }

    /**
     * @return the target_worker
     */
    public String getTarget_worker() {
        return target_worker;
    }

    /**
     * @param target_worker the target_worker to set
     */
    public void setTarget_worker(String target_worker) {
        this.target_worker = target_worker;
    }

    /**
     * @return the source_endpoint
     */
    public String getSource_endpoint() {
        return source_endpoint;
    }

    /**
     * @param source_endpoint the source_endpoint to set
     */
    public void setSource_endpoint(String source_endpoint) {
        this.source_endpoint = source_endpoint;
    }

    /**
     * @return the target_endpoint
     */
    public String getTarget_endpoint() {
        return target_endpoint;
    }

    /**
     * @param target_endpoint the target_endpoint to set
     */
    public void setTarget_endpoint(String target_endpoint) {
        this.target_endpoint = target_endpoint;
    }

    /**
     * @return the instance
     */
    public int getTarget_instance() {
        return target_instance;
    }

    /**
     * @param instance the instance to set
     */
    public void setTarget_instance(int instance) {
        this.target_instance = instance;
    }

    /**
     * @return the mapping
     */
    public int getMapping() {
        return mapping;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(int mapping) {
        this.mapping = mapping;
    }

    /**
     * @return the queue_name
     */
    public String getQueue_name() {
        return queue_name;
    }

    /**
     * @param queue_name the queue_name to set
     */
    public void setQueue_name(String queue_name) {
        this.queue_name = queue_name;
    }

    /**
     * @return the exchange_name
     */
    public String getExchange_name() {
        return exchange_name;
    }

    /**
     * @param exchange_name the exchange_name to set
     */
    public void setExchange_name(String exchange_name) {
        this.exchange_name = exchange_name;
    }

    /**
     * @return the routing_keys
     */
    public HashSet<String> getRouting_keys() {
        return routing_keys;
    }
    
    public JSONArray getRouting_keys_json() {
        JSONArray json = new JSONArray();
        for (String rk : routing_keys)
            json.put(rk);
        
        return json;
    }

    /**
     * @param routing_keys the routing_keys to set
     */
    public void setRouting_keys(HashSet<String> routing_keys) {
        this.routing_keys = routing_keys;
    }
}
