package gr.iti.openzoo.ui;

import java.util.ArrayList;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class WarFile {
    
    private String filename;
    private String folder;
    private String version;
    private ArrayList<String> requires;
    private String status;
    
    private static Utilities util = new Utilities();

    public WarFile(String fi, String fo, String v, String s, ArrayList<String> r)
    {
        filename = fi;
        folder = fo;
        version = v;        
        status = s;
        requires = new ArrayList<>();
        requires.addAll(r);
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
    
    public JSONObject toJSON()
    {
        JSONObject ret = new JSONObject();
        
        try
        {    
            ret.put("filename", filename);
            ret.put("folder", folder);
            ret.put("version", version);
            ret.put("status", status);
            
            JSONArray jarr = new JSONArray();
            for (String ss : requires)
                jarr.put(ss);
            ret.put("requires", jarr);
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in toJSON: " + e);
        }
        
        return ret;
    }

}
