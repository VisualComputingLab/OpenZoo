package gr.iti.openzoo.ui;

import java.util.ArrayList;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class TopologyService {

    private String name;
    private String description;
    private String instanceOf;
    private String belongsToTopology;
    private ArrayList<String> requires;

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
     * @return the instanceOf
     */
    public String getInstanceOf() {
        return instanceOf;
    }

    /**
     * @param instanceOf the instanceOf to set
     */
    public void setInstanceOf(String instanceOf) {
        this.instanceOf = instanceOf;
    }

    /**
     * @return the belongsToTopology
     */
    public String getBelongsToTopology() {
        return belongsToTopology;
    }

    /**
     * @param belongsToTopology the belongsToTopology to set
     */
    public void setBelongsToTopology(String belongsToTopology) {
        this.belongsToTopology = belongsToTopology;
    }

    /**
     * @return the requires
     */
    public ArrayList<String> getRequires() {
        return requires;
    }

    /**
     * @param requires the requires to set
     */
    public void setRequires(ArrayList<String> requires) {
        this.requires = requires;
    }
}
