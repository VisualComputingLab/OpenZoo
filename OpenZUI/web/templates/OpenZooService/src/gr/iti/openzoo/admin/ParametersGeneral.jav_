package gr.iti.openzoo.admin;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ParametersGeneral implements java.io.Serializable {
    
    private String name = null;
    private String path = null;
    private String description = null;
    private String realPath = null;
    private String topologyID = null;
    private String componentID = null;
    private String instanceID = null;
    private Integer numOfWorkersPerCore = 1;

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
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
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

    /**
     * @return the realPath
     */
    public String getRealPath() {
        return realPath;
    }

    /**
     * @param realPath the realPath to set
     */
    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getComponentID() {
        return componentID;
    }

    public void setTopologyID(String topologyID) {
        this.topologyID = topologyID;
    }
    
    /**
     * @return the id
     */
    public String getTopologyID() {
        return topologyID;
    }

    /**
     * @param id the id to set
     */
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }
    
    public String getInstanceID() {
        return instanceID;
    }

    /**
     * @param id the id to set
     */
    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    /**
     * @return the numOfWorkersPerCore
     */
    public Integer getNumOfWorkersPerCore() {
        return numOfWorkersPerCore;
    }

    /**
     * @param numOfWorkersPerCore the numOfInstancesPerCore to set
     */
    public void setNumOfWorkersPerCore(Integer numOfWorkersPerCore) {
        this.numOfWorkersPerCore = numOfWorkersPerCore;
    }
    
    @Override
    public String toString()
    {
        String out = "-- GENERAL PARAMETERS --\n";
        out += "Topology ID: " + topologyID + "\n";
        out += "Component ID: " + componentID + "\n";
        out += "Instance ID: " + instanceID + "\n";
        out += "Name: " + name + "\n";
        out += "Path: " + path + "\n";
        out += "Description: " + description + "\n";
        out += "Real path: " + realPath + "\n";
        out += "Num of workers per core: " + numOfWorkersPerCore + "\n\n";
        
        return out;
    }
}
