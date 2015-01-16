package gr.iti.openzoo.admin;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ParametersKeyValue implements java.io.Serializable {

    private String host = null;
    private Integer port = -1;

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }
    
    @Override
    public String toString()
    {
        String out = "-- KEYVALUE PARAMETERS --\n";
        out += "Host: " + host + "\n";
        out += "Port: " + port + "\n\n";
        
        return out;
    }
}
