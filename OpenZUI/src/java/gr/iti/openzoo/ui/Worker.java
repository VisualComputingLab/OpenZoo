package gr.iti.openzoo.ui;

import java.util.ArrayList;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Worker {

    private String worker_id;
    private String component_id;
    private ArrayList<String> in;
    private ArrayList<String> out;
    
    public Worker(JSONObject w, String c_id)
    {
        try
        {
            worker_id = w.getString("worker_id");
            component_id = c_id;
            in = new ArrayList<>();
            out = new ArrayList<>();
            
            JSONArray endpoints = w.getJSONArray("endpoints");
            JSONObject ep;
            
            for (int i = 0; i < endpoints.length(); i++)
            {
                ep = endpoints.getJSONObject(i);
                if (ep.getString("type").equalsIgnoreCase("in"))
                    in.add(ep.getString("endpoint_id"));
                else if (ep.getString("type").equalsIgnoreCase("out"))
                    out.add(ep.getString("endpoint_id"));
            }
        }
        catch (JSONException ex)
        {
            System.err.println("JSONException in Worker constr: " + ex);
        }
    }

    /**
     * @return the worker_id
     */
    public String getWorker_id() {
        return worker_id;
    }

    /**
     * @return the in
     */
    public ArrayList<String> getIn() {
        return in;
    }

    /**
     * @return the out
     */
    public ArrayList<String> getOut() {
        return out;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'worker_id':'");
        sb.append(worker_id);
        sb.append("','endpoints':[");
        for (String ss : in)
        {
            sb.append("{'endpoint_id':'");
            sb.append(ss);
            sb.append("','type':'in'},");
        }
        for (String ss : out)
        {
            sb.append("{'endpoint_id':'");
            sb.append(ss);
            sb.append("','type':'out'},");
        }
        if (sb.charAt(sb.length()-1) == ',')
            sb.deleteCharAt(sb.length()-1);
            
        sb.append("]}");
        
        return sb.toString();
    }
    
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        JSONArray jarr = new JSONArray();
        JSONObject endp;
        
        try
        {
            json.put("worker_id", worker_id);
            
            for (String ss : in)
            {
                endp = new JSONObject();
                endp.put("endpoint_id", ss);
                endp.put("type", "in");
                jarr.put(endp);
            }
            for (String ss : out)
            {
                endp = new JSONObject();
                endp.put("endpoint_id", ss);
                endp.put("type", "out");
                jarr.put(endp);
            }
            
            json.put("endpoints", jarr);
        }
        catch (JSONException e)
        {
            System.err.println("JSONException during creating worker json: " + e);
        }
        
        return json;
    }

    /**
     * @return the component_id
     */
    public String getComponent_id() {
        return component_id;
    }
}
