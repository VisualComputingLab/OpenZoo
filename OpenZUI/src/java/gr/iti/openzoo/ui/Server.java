package gr.iti.openzoo.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Server {

    private String name;
    private String address;
    private int port;
    private String user;
    private String passwd;
    private String status;
    
    private static Utilities util = new Utilities();

    public Server(String n, String a, int p, String u, String pw)
    {
        name = n;
        address = a;
        port = p;
        user = u;
        passwd = pw;
        status = "inactive";
        
        statusUpdated();
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
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
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
    
    @Override
    public String toString()
    {
        return name + " " + address + " " + port + " " + user + " " + passwd + " " + status;
    }
    
    public JSONObject toJSON()
    {
        JSONObject ret = new JSONObject();
        
        try
        {
            ret.put("name", name);
            ret.put("address", address);
            ret.put("port", port);
            ret.put("user", user);
            ret.put("passwd", passwd);
            ret.put("status", status);
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in toJSON: " + e);
        }
        
        return ret;
    }
    
    public boolean statusUpdated()
    {
        // check status
        // return true if status changed
        String oldStatus = status;
        String newStatus;
        String output = null;
        try
        {
            output = util.callGET(new URL("http://" + address + ":" + port));
            //Logger.getLogger(Server.class.getName()).log(Level.INFO, output);
            if (output == null || output.equalsIgnoreCase("zero"))
                newStatus = "inactive";
            else newStatus = "active";
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, output, ex);
            newStatus = "inactive";
        }
                
        if (oldStatus.equals(newStatus)) return false;
        
        status = newStatus;
        
        return true;
    }
    
    public boolean isActive()
    {
        if (status.equalsIgnoreCase("active"))
            return true;
        
        return false;
    }
}
