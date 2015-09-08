package gr.iti.openzoo.ui;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Topology {

    private String name;
    private String description;
    
    private String rabbit_host;
    private int rabbit_port;
    private String rabbit_user;
    private String rabbit_passwd;
    private String mongo_host;
    private int mongo_port;
    private String mongo_user;
    private String mongo_passwd;
    
    private static Utilities util = new Utilities();

    public Topology(String n, String d, String rh, int rp, String ru, String rps, String mh, int mp, String mu, String mps)
    {
        name = n;
        description = d;
        rabbit_host = rh;
        rabbit_port = rp;
        rabbit_user = ru;
        rabbit_passwd = rps;
        mongo_host = mh;
        mongo_port = mp;
        mongo_user = mu;
        mongo_passwd = mps;
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
     * @return the rabbit_host
     */
    public String getRabbit_host() {
        return rabbit_host;
    }

    /**
     * @param rabbit_host the rabbit_host to set
     */
    public void setRabbit_host(String rabbit_host) {
        this.rabbit_host = rabbit_host;
    }

    /**
     * @return the rabbit_port
     */
    public int getRabbit_port() {
        return rabbit_port;
    }

    /**
     * @param rabbit_port the rabbit_port to set
     */
    public void setRabbit_port(int rabbit_port) {
        this.rabbit_port = rabbit_port;
    }

    /**
     * @return the rabbit_user
     */
    public String getRabbit_user() {
        return rabbit_user;
    }

    /**
     * @param rabbit_user the rabbit_user to set
     */
    public void setRabbit_user(String rabbit_user) {
        this.rabbit_user = rabbit_user;
    }

    /**
     * @return the rabbit_passwd
     */
    public String getRabbit_passwd() {
        return rabbit_passwd;
    }

    /**
     * @param rabbit_passwd the rabbit_passwd to set
     */
    public void setRabbit_passwd(String rabbit_passwd) {
        this.rabbit_passwd = rabbit_passwd;
    }

    /**
     * @return the mongo_host
     */
    public String getMongo_host() {
        return mongo_host;
    }

    /**
     * @param mongo_host the mongo_host to set
     */
    public void setMongo_host(String mongo_host) {
        this.mongo_host = mongo_host;
    }

    /**
     * @return the mongo_port
     */
    public int getMongo_port() {
        return mongo_port;
    }

    /**
     * @param mongo_port the mongo_port to set
     */
    public void setMongo_port(int mongo_port) {
        this.mongo_port = mongo_port;
    }

    /**
     * @return the mongo_user
     */
    public String getMongo_user() {
        return mongo_user;
    }

    /**
     * @param mongo_user the mongo_user to set
     */
    public void setMongo_user(String mongo_user) {
        this.mongo_user = mongo_user;
    }

    /**
     * @return the mongo_passwd
     */
    public String getMongo_passwd() {
        return mongo_passwd;
    }

    /**
     * @param mongo_passwd the mongo_passwd to set
     */
    public void setMongo_passwd(String mongo_passwd) {
        this.mongo_passwd = mongo_passwd;
    }

    @Override
    public String toString()
    {
        return name + " " + description + " " + rabbit_host + " " + rabbit_port + " " + rabbit_user + " " + rabbit_passwd + " " + mongo_host + " " + mongo_port + " " + mongo_user + " " + mongo_passwd;
    }
    
    public JSONObject toJSON()
    {
        JSONObject ret = new JSONObject();
        
        try
        {            
            ret.put("name", name);
            ret.put("description", description);
            
            JSONObject rabbit = new JSONObject();
            rabbit.put("host", rabbit_host);
            rabbit.put("port", rabbit_port);
            rabbit.put("user", rabbit_user);
            rabbit.put("passwd", rabbit_passwd);
            ret.put("rabbit", rabbit);
            
            JSONObject mongo = new JSONObject();
            mongo.put("host", mongo_host);
            mongo.put("port", mongo_port);
            mongo.put("user", mongo_user);
            mongo.put("passwd", mongo_passwd);
            ret.put("mongo", mongo);
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in toJSON: " + e);
        }
        
        return ret;
    }
}
