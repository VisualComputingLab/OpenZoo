package gr.iti.openzoo.admin;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ParametersKeyValue implements java.io.Serializable {
    
    private String host = null;
    private Integer port = -1;
    private String user = null;
    private String passwd = null;
    private String db = null;

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
    
    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the passwd
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * @param passwd the passwd to set
     */
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    /**
     * @return the db
     */
    public String getDb() {
        return db;
    }

    /**
     * @param db the db to set
     */
    public void setDb(String db) {
        this.db = db;
    }
    
    @Override
    public String toString()
    {
        String out = "-- KEYVALUE PARAMETERS --\n";
        out += "Host: " + host + "\n";
        out += "Port: " + port + "\n\n";
        out += "User: " + user + "\n";
        out += "Password: " + passwd + "\n";
        out += "Database: " + db + "\n\n";
        
        return out;
    }
}
