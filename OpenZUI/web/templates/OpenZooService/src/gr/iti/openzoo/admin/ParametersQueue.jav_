package gr.iti.openzoo.admin;

import java.util.HashSet;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ParametersQueue {

    public enum Mapping { AVAIL, ALL, ROUTE }
    
    private Mapping mapping = Mapping.AVAIL;
    private String queue_name = null;
    private String exchange_name = null;
    private HashSet<String> routing_keys = null;
    
    public ParametersQueue(JSONObject json) throws JSONException
    {        
        int i_map = json.getInt("mapping");
        
        switch (i_map)
        {
            case 2: mapping = Mapping.ROUTE;
                    exchange_name = json.getString("exchange_name");
                    JSONArray rk = json.getJSONArray("routing_keys");
                    if (rk != null)
                    {
                        routing_keys = new HashSet<>();
                        for (int i = 0; i < rk.length(); i++)
                        {
                            routing_keys.add(rk.optString(i));
                        }
                    }
                    else
                    {
                        throw new JSONException("routing_keys parameter is null");
                    }
                    break;
            case 1: mapping = Mapping.ALL;
                    exchange_name = json.getString("exchange_name");
                    break;
            default:mapping = Mapping.AVAIL;
                    queue_name = json.getString("queue_name");
                    break;
        }        
    }
    
    public Mapping getMapping()
    {
        return mapping;
    }
    
    public String getQueueName()
    {
        return queue_name;
    }
    
    public String getExchangeName()
    {
        return exchange_name;
    }
        
    public HashSet<String> getRoutingKeys()
    {
        return routing_keys;
    }
}
