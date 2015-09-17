package gr.iti.openzoo.ui;

import java.util.ArrayList;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServerResources {

    private static double MAX_CPU_USAGE = 0.8; // CPU usage should be less than 80 %
    private static double MAX_HEAP_USAGE = 0.8; // Heap memory usage should be less than 80 %
    private static double MIN_SPACE_BYTE = 1024*1024*1024; // At least 1 GB should be free
    
    private String servername;
    private String os;                  // Linux, Windows...
    private double systemCpuLoad;       // [0,1]
//    private long heapMemoryTotal;       // bytes
//    private long heapMemoryFree;        // bytes
//    private long nonHeapMemoryTotal;    // bytes
//    private long nonHeapMemoryFree;     // bytes
//    private long spaceFree;             // bytes
//    private long spaceTotal;            // bytes
    private double heapMemoryUsage;       // [0,1]
    private long spaceFree;            // [0,1]
    ArrayList<String> deployedServices;
    
    public ServerResources(String name, JSONObject json)
    {
        servername = name;
        update(json);
    }
    
    public void update(JSONObject json)
    {
        try
        {
            os = json.getString("name");
            systemCpuLoad = json.getJSONObject("cpu").getDouble("systemCpuLoad");
            if (json.getJSONObject("mem").getJSONObject("heap").getLong("max") > 0)
                heapMemoryUsage = json.getJSONObject("mem").getJSONObject("heap").getLong("used") / json.getJSONObject("mem").getJSONObject("heap").getLong("max");
            else heapMemoryUsage = 1.0;
            spaceFree = json.getJSONObject("space").getLong("free");
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in ServerResources.loadFromJson: " + e);
        }
    }
    
    public void addDeployedServices(String ds)
    {
        System.out.println("- DS -");
        System.out.println(ds);
        System.out.println("- DS end -");
    }
    
    public boolean isServiceDeployed(String serv)
    {
        if (deployedServices == null) return false;
        
        return deployedServices.contains(serv);
    }
    
    public boolean areResourcesAvailable()
    {
        if (getSystemCpuLoad() > MAX_CPU_USAGE || heapMemoryUsage > MAX_HEAP_USAGE || spaceFree < MIN_SPACE_BYTE)
            return false;
        
        return true;
    }

    /**
     * @return the servername
     */
    public String getServername() {
        return servername;
    }

    /**
     * @param servername the servername to set
     */
    public void setServername(String servername) {
        this.servername = servername;
    }

    /**
     * @return the systemCpuLoad
     */
    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }
}
