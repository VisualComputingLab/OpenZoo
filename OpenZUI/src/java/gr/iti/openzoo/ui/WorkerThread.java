package gr.iti.openzoo.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class WorkerThread implements Runnable
{
    private JSONObject conf_object;
    private int index;
    private String servername;
    private WarFile war;
    private JSONObject server_conf;
    private String repository;
    private String topology_id;
    private KeyValueCommunication kv;
    private List<String> logs;
    
    private String action;
    
    public WorkerThread(String act, String srvname, WarFile wf, JSONObject srvconf, String repo, JSONObject j, int i, String toponame, KeyValueCommunication kvc, List<String> loglist)
    {
        action = act;
        servername = srvname;
        war = wf;
        server_conf = srvconf;
        repository = repo;
        conf_object = j;
        index = i;
        topology_id = toponame;
        kv = kvc;
        logs = loglist;
    }

    @Override
    public void run() {
        switch (action)
        {
            case "deploy":      deploy(); break;
            case "undeploy":    undeploy(); break;
            case "redeploy":    redeploy(); break;
            case "start": 
            case "stop":        runCommand(); break;
        }
    }
    
    private void deploy()
    {
        // open war file
        // include kv_host, kv_port, topo_name, instance_id into config.json
        File f_copy;
        JSONObject config = Utilities.readJSONFromWAR(repository + "/" + war.getFilename(), "config.json");
        try
        {
            config.put("keyvalue", new JSONObject().put("host", kv.getKVHost()).put("port", kv.getKVPort()));
            config.put("instance_id", server_conf.getInt("instance_id"));
            config.put("topology_id", topology_id);

            File f = new File(repository + "/" + war.getFilename());
            f_copy = new File(repository + "/" + war.getFilename() + "_" + servername + ".war");
            Files.copy(f.toPath(), f_copy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Updating config.json in " + f_copy.getAbsolutePath());
            Utilities.writeJSONToWAR(f_copy.getAbsolutePath(), "config.json", config);

            JSONObject service_conf;
            
            if (conf_object.has(war.getComponent_id()))
            {
                service_conf = conf_object.getJSONObject(war.getComponent_id());
                service_conf.put(servername, server_conf);
            }
            else
            {
                service_conf = new JSONObject();
                service_conf.put(servername, server_conf);
                conf_object.put(war.getComponent_id(), service_conf);
            }
        }
        catch (IOException | JSONException e)
        {
            System.err.println("Exception during injecting config.json to " + war.getComponent_id() + " for " + servername + ":" + e);
            logs.add("ERROR:" + "Exception during injecting config.json to " + war.getComponent_id() + " for " + servername + ":" + e);
            return;
        }


        // deploy services according to configuration
        Server srv = kv.getServer(servername);
        JSONObject outjson = deployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), f_copy.getAbsolutePath(), "/" + war.getComponent_id());

        // on error, print and go to next
        // on success, set stati to 'installed'
        if (outjson == null)
        {
            System.err.println("Service deployment for " + war.getComponent_id() + " on " + servername + " failed");
            logs.add("ERROR:" + "Service deployment for " + war.getComponent_id() + " on " + servername + " failed");
            return;
        }
        
        try
        {
            if (outjson.getString("status").equalsIgnoreCase("success"))
            {
                conf_object.getJSONObject(war.getComponent_id()).getJSONObject(servername).put("status", "installed");
            }
            else
            {
                System.err.println("Service deployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                logs.add("ERROR:" + "Service deployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                return;
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException during updating conf_object for " + war.getComponent_id() + " on " + servername + ": " + e);
            logs.add("ERROR:" + "JSONException during updating conf_object for " + war.getComponent_id() + " on " + servername + ": " + e);
            return;
        }
        
        logs.add("INFO:" + "Deployed " + war.getComponent_id() + " on " + servername);
    }
    
    private void undeploy()
    {
        Server srv = kv.getServer(servername);
        JSONObject outjson = undeployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), "/" + war.getComponent_id());

        // on error, print and exit
        // on success, set status to 'void'
        
        if (outjson == null)
        {
            System.err.println("Service undeployment for " + war.getComponent_id() + " on " + servername + " failed");
            logs.add("ERROR:" + "Service undeployment for " + war.getComponent_id() + " on " + servername + " failed");
            return;
        }
        
        try
        {
            if (outjson.getString("status").equalsIgnoreCase("success"))
            {
                server_conf.put("status", "void");
                conf_object.put(war.getComponent_id(), servername);
            }
            else
            {
                System.err.println("Service undeployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                logs.add("ERROR:" + "Service undeployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                return;
            }
        }
        catch (JSONException e)
        {
            System.out.println("Exception during updating conf_object of " + war.getComponent_id() + " for " + servername + ": " + e);
            logs.add("ERROR:" + "Exception during updating conf_object of " + war.getComponent_id() + " for " + servername + ": " + e);
            return;
        }
        
        logs.add("INFO:" + "Undeployed " + war.getComponent_id() + " on " + servername);
    }

    private void redeploy()
    {
        Server srv = kv.getServer(servername);
        JSONObject outjson = undeployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), "/" + war.getComponent_id());

        // on error, print and exit
        // on success, set status to 'void'
        
        if (outjson == null)
        {
            System.err.println("Service undeployment for " + war.getComponent_id() + " on " + servername + " failed");
            logs.add("ERROR:" + "Service undeployment for " + war.getComponent_id() + " on " + servername + " failed");
            return;
        }
        
        try
        {
            if (outjson.getString("status").equalsIgnoreCase("success"))
            {
                server_conf.put("status", "void");
                
                //-
                // open war file
                // include kv_host, kv_port, topo_name, instance_id into config.json
                File f_copy;
                JSONObject config = Utilities.readJSONFromWAR(repository + "/" + war.getFilename(), "config.json");
                try
                {
                    config.put("keyvalue", new JSONObject().put("host", kv.getKVHost()).put("port", kv.getKVPort()));
                    config.put("instance_id", server_conf.getInt("instance_id"));
                    config.put("topology_id", topology_id);

                    File f = new File(repository + "/" + war.getFilename());
                    f_copy = new File(repository + "/" + war.getFilename() + "_" + servername + ".war");
                    Files.copy(f.toPath(), f_copy.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("Updating config.json in " + f_copy.getAbsolutePath());
                    Utilities.writeJSONToWAR(f_copy.getAbsolutePath(), "config.json", config);
                }
                catch (IOException | JSONException e)
                {
                    System.err.println("Exception during injecting config.json to " + war.getComponent_id() + " for " + servername + ":" + e);
                    logs.add("ERROR:" + "Exception during injecting config.json to " + war.getComponent_id() + " for " + servername + ":" + e);
                    return;
                }


                // deploy services according to configuration
                outjson = deployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), f_copy.getAbsolutePath(), "/" + war.getComponent_id());

                // on error, print and go to next
                // on success, set stati to 'installed'
                if (outjson == null)
                {
                    System.err.println("Service deployment for " + war.getComponent_id() + " on " + servername + " failed");
                    logs.add("ERROR:" + "Service deployment for " + war.getComponent_id() + " on " + servername + " failed");
                    return;
                }

                try
                {
                    if (outjson.getString("status").equalsIgnoreCase("success"))
                    {
                        server_conf.put("status", "void");
                    }
                    else
                    {
                        System.err.println("Service deployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                        logs.add("ERROR:" + "Service deployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                        return;
                    }
                }
                catch (JSONException e)
                {
                    System.err.println("JSONException during updating conf_object for " + war.getComponent_id() + " on " + servername + ": " + e);
                    logs.add("ERROR:" + "JSONException during updating conf_object for " + war.getComponent_id() + " on " + servername + ": " + e);
                    return;
                }
                //-
            }
            else
            {
                System.err.println("Service undeployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                logs.add("ERROR:" + "Service undeployment for " + war.getComponent_id() + " on " + servername + " failed with: " + outjson.getString("message"));
                return;
            }
        }
        catch (JSONException e)
        {
            System.out.println("Exception during updating conf_object of " + war.getComponent_id() + " for " + servername + ": " + e);
            logs.add("ERROR:" + "Exception during updating conf_object of " + war.getComponent_id() + " for " + servername + ": " + e);
            return;
        }
        
        logs.add("INFO:" + "Redeployed " + war.getComponent_id() + " on " + servername);
    }
    
    private void runCommand()
    {
        Server srv = kv.getServer(servername);
        Utilities util = new Utilities();
        
        try
        {
            URL url = new URL("http://" + srv.getAddress() + ":" + srv.getPort() + "/" + war.getComponent_id() + war.getService_path() + "?action=" + action);
            JSONObject output = new JSONObject(util.callGET(url, null, null));

            // on error, print and exit
            // on success, set stati to 'running'
            if (output.has("error"))
            {
                System.err.println("Calling " + action + " on service " + war.getComponent_id() + " on server " + servername + " failed with output: " + output.toString(4));
                logs.add("ERROR:" + "Calling " + action + " on service " + war.getComponent_id() + " on server " + servername + " failed");
            }
            else
            {
                switch (action)
                {
                    case "start":
                        server_conf.put("status", "running");
                        break;

                    case "stop":
                        server_conf.put("status", "installed");
                        break;
                }
            }
        }
        catch (JSONException | IOException ex)
        {
            System.err.println("Running action " + action + " for " + war.getComponent_id() + " on " + servername + " failed: " + ex);
            logs.add("ERROR:" + "Running action " + action + " for " + war.getComponent_id() + " on " + servername + " failed: " + ex);
            return;
        }
        
        logs.add("INFO:" + "Run command " + action + " for " + war.getComponent_id() + " on " + servername);
    }
    
    private JSONObject deployService(String httpserverandport, String servercredentials, String warfilepath, String webservicepath)
    {
        // httpserverandport: server to call, e.g. "http://localhost:8080"
        // servercredentials: server credentials, e.g. "admin:passwd"
        // warfilepath: path to the WAR file
        // webservicepath: service path, e.g. "/SEIndexShardService"
        
        String output;
        JSONObject outjson = new JSONObject();
        
        try
        {
            try
            {
                URL url = new URL(httpserverandport + "/manager/text/deploy?path=" + webservicepath + "&update=true");
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                //httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
                httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("PUT");
                copyInputStream(new FileInputStream(warfilepath), httpCon.getOutputStream());
                output = convertStreamToString(httpCon.getInputStream());
                outjson.put("status", "success");
                outjson.put("message", output);
                outjson.put("response_code", httpCon.getResponseCode());
                outjson.put("response_message", httpCon.getResponseMessage());
            }
            catch (IOException e)
            {
                output = "IOException during web service deployment: " + e;
                outjson.put("status", "failure");
                outjson.put("message", output);
            }
        }
        catch (JSONException ex)
        {
            System.err.println("JSONException in deployService: " + ex);
            return null;
        }
        
        return outjson;
    }
    
    public JSONObject undeployService(String httpserverandport, String servercredentials, String webservicepath)
    {
        String output;
        JSONObject outjson = new JSONObject();
        
        try
        {
            try
            {
                URL url = new URL(httpserverandport + "/manager/text/undeploy?path=" + webservicepath);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                //httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
                httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("GET");
                output = convertStreamToString(httpCon.getInputStream());
                outjson.put("status", "success");
                outjson.put("message", output);
                outjson.put("response_code", httpCon.getResponseCode());
                outjson.put("response_message", httpCon.getResponseMessage());
            }
            catch (IOException e)
            {
                output = "IOException during web service undeployment: " + e;
                outjson.put("status", "failure");
                outjson.put("message", output);
            }
        }
        catch (JSONException ex)
        {
            System.err.println("JSONException in undeployService: " + ex);
            return null;
        }
        
        return outjson;
    }
    
    private static String convertStreamToString(InputStream is) throws IOException {
        //
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        //
        if (is != null) 
        {
            Writer writer = new StringWriter();
 
            char[] buffer = new char[1024];
            try 
            {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) 
                {
                    writer.write(buffer, 0, n);
                }
            } 
            finally 
            {
                is.close();
            }
            
            return writer.toString();
        } 
        else 
        {       
            return "";
        }
    }
    
    private static boolean copyInputStream(InputStream in, OutputStream out)
    {
        byte[] buffer = new byte[1024];
        int len;

        try
        {
            while((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);

            in.close();
            out.close();
        }
        catch(IOException ioe)
        {
            System.err.println("Error while unziping: " + ioe);
            return false;
        }

        return true;
    }
}
