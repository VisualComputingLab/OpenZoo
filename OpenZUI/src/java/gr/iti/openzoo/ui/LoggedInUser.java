package gr.iti.openzoo.ui;

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class LoggedInUser {

    private Utilities util = new Utilities();
    
    private String email = null;
    private String passwordSHA1 = null;
    private String role = null;
    private boolean loggedIn = false;
            
    public LoggedInUser(String u, String p, String configPath)
    {
        email = u;
        
        JSONObject properties = util.getJSONFromFile(configPath);
        try 
        {
            String demouser = properties.getJSONObject("demouser").getString("username");
            String demopass = properties.getJSONObject("demouser").getString("passwd");
            
            if (email.equals(demouser) && p.equals(demopass))
            {
                loggedIn = true;
            }
            else
            {
                loggedIn = false;
            }
            
            //loggedIn = isAuthorized(p);
        }
        catch (JSONException ex) 
        {
            System.err.println("ERROR retrieving keyValue server: " + ex);
        }   
    }
    
    public LoggedInUser()
    {
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        
        if (password != null && !password.isEmpty())
            loggedIn = isAuthorized(password);
        else loggedIn = false;
    }
    
    public Boolean isLoggedIn() {
        return loggedIn;
    }

    public void logOut()
    {
        email = null;
        passwordSHA1 = null;
        setRole(null);
        loggedIn = false;
    }
    
    private boolean isAuthorized(String passwd)
    {        
        String p2 = DigestUtils.sha1Hex(passwd);
        if (passwordSHA1.equalsIgnoreCase(p2))
            return true;
        
        return false;
    }
    
    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
}
