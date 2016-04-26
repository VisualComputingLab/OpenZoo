package gr.iti.openzoo.ui;

import java.util.ArrayList;
import java.util.TreeMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class MessageStatistics {

    private TreeMap<String, ArrayList<Long>> data;
    
    public MessageStatistics()
    {
        data = new TreeMap<>();
    }
    
    public void addEndpointMessages(String key, Long val)
    {
        ArrayList<Long> sub = data.get(key);
        
        if (sub == null)
        {
            sub = new ArrayList<>();
            sub.add(0L);
            sub.add(0L);
            data.put(key, sub);
        }
                        
        sub.set(0, sub.get(0) + val);
    }
    
    public void addEndpointBytes(String key, Long val)
    {
        ArrayList<Long> sub = data.get(key);
         
        if (sub == null)
        {
            sub = new ArrayList<>();
            sub.add(0L);
            sub.add(0L);
            data.put(key, sub);
        }
        
        sub.set(1, sub.get(1) + val);
    }
    
    public JSONObject getJSON()
    {
        JSONObject response = new JSONObject();
        String [] split;
        String comp, wrk, ep, inst;
        JSONObject j_comp, j_inst;
        
        try
        {
            for (String key : data.keySet())
            {
                split = key.split(":");
                if (split.length != 4)
                {
                    System.err.println("Wrong key format in message statistics: " + key);
                    return null;
                }
                comp = split[0];
                wrk = split[1];
                wrk = wrk.substring(wrk.lastIndexOf(".")+1);
                ep =  wrk + ":" + split[2];
                inst = split[3];

                j_comp = response.optJSONObject(comp);
                
                if (j_comp == null)
                {
                    j_comp = new JSONObject();
                }
                
                j_inst = j_comp.optJSONObject(inst);
                
                if (j_inst == null)
                {
                    j_inst = new JSONObject();
                }
                
                j_inst.put(ep, data.get(key));
                j_comp.put(inst, j_inst);
                response.put(comp, j_comp);
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException while formating message statistics: " + e);
        }
        
        return response;
    }
}
