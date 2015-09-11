package gr.iti.openzoo.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class WarFile {
    
    private String component_id;
    private String name;
    private String service_path;
    private String description;
    private String filename;
    private String folder;
    private String version;
    private String status;
    private ArrayList<String> requires;
    private ArrayList<Worker> workers;
    
    private static Utilities util = new Utilities();
    
    public WarFile(String fi, String fo, String v, String s, JSONObject config)
    {
        filename = fi;
        folder = fo;
        version = v;        
        status = s;
        
        requires = new ArrayList<>();
        workers = new ArrayList<>();
        
        if (config != null)
        {
            try
            {
                JSONObject service = config.getJSONObject("service");
                if (service != null)
                {
                    component_id = service.getString("component_id");
                    name = service.getString("name");
                    service_path = service.getString("path");
                    description = service.getString("description");
                }

                JSONArray jarr_requires = config.optJSONArray("requires");
                if (jarr_requires != null)
                {
                    for (int i = 0; i < jarr_requires.length(); i++)
                        requires.add(jarr_requires.getString(i));
                }

                JSONArray jarr_workers = config.optJSONArray("workers");
                if (jarr_workers != null)
                {
                    for (int i = 0; i < jarr_workers.length(); i++)
                    {
                        Worker w = new Worker(jarr_workers.getJSONObject(i));
                        workers.add(w);
                    }
                }

            }
            catch (JSONException ex)
            {
                System.err.println("JSONException while extracting service requirements: " + ex);
            }
        }
    }
        
    public WarFile(Map<String, String> prop)
    {
        component_id = prop.get("component_id");
        name = prop.get("name");
        service_path = prop.get("service_path");
        description = prop.get("description");        
        filename = prop.get("filename");
        folder = prop.get("folder");
        version = prop.get("version");        
        status = prop.get("status");
        
        requires = new ArrayList<>();
        String req = prop.get("requires");
        if (req != null)
        {
            requires.addAll(Arrays.asList(req.split(" ")));
        }
        
        workers = new ArrayList<>();
        String work = prop.get("workers");
        if (work != null)
        {
            try
            {
                String[] split = work.split(" ");
                for (int i = 0; i < split.length; i++)
                {
                    JSONObject json = new JSONObject(split[i]);
                    Worker w = new Worker(json);
                    workers.add(w);
                }
            }
            catch (JSONException ex)
            {
                System.err.println("JSONException in WarFile constr: " + ex);
            }
        }
    }
    
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the requires
     */
    public ArrayList<String> getRequires() {
        return requires;
    }
    
    public ArrayList<Worker> getWorkers() {
        return workers;
    }
    
    public JSONObject toJSON()
    {
        JSONObject ret = new JSONObject();
        
        try
        {    
            ret.put("component_id", component_id);
            ret.put("name", name);
            ret.put("service_path", service_path);
            ret.put("description", description);
            ret.put("filename", filename);
            ret.put("folder", folder);
            ret.put("version", version);
            ret.put("status", status);
            
            JSONArray jarr = new JSONArray();
            for (String ss : requires)
                jarr.put(ss);
            ret.put("requires", requires);
            
            JSONArray jarrw = new JSONArray();
            JSONObject json, jsonep;
            JSONArray jarrep;
            for (Worker w : workers)
            {
                json = new JSONObject();
                json.put("worker_id", w.getWorker_id());
                jarrep = new JSONArray();
                for (String ss : w.getIn())
                {
                    jsonep = new JSONObject();
                    jsonep.put("type", "in");
                    jsonep.put("endpoint_id", ss);
                    jarrep.put(jsonep);
                }
                for (String ss : w.getOut())
                {
                    jsonep = new JSONObject();
                    jsonep.put("type", "out");
                    jsonep.put("endpoint_id", ss);
                    jarrep.put(jsonep);
                }
                json.put("endpoints", jarrep);
                jarrw.put(json);
            }
            ret.put("workers", jarrw);
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in toJSON: " + e);
        }
        
        return ret;
    }

    /**
     * @return the component_id
     */
    public String getComponent_id() {
        return component_id;
    }

    /**
     * @param component_id the component_id to set
     */
    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the service_path
     */
    public String getService_path() {
        return service_path;
    }

    /**
     * @param service_path the service_path to set
     */
    public void setService_path(String service_path) {
        this.service_path = service_path;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}

