package gr.iti.openzoo.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Deployer {

    private Utilities util = new Utilities();
    private static KeyValueCommunication kv;
    private String repo;
    
    public Deployer(JSONObject properties)
    {
        try 
        {        
            kv = new KeyValueCommunication(properties.getJSONObject("keyvalue").getString("host"), properties.getJSONObject("keyvalue").getInt("port"));
            repo = properties.getString("localRepository");
        }
        catch (JSONException ex) 
        {
            System.err.println("ERROR retrieving keyValue server: " + ex);
        }
    }
    
    public String deployTopology(String topo_name)
    {
        // get topology json from kv
        Topology topo = kv.getTopology(topo_name);
        
        // get servers from kv
        ArrayList<Server> servers = kv.getServers();
        ArrayList<ServerResources> resources = new ArrayList<>();
        ArrayList<TopologyGraphNode> nodes = topo.getNodes();
        
        // for each service instance, choose a server based on server statistics and previously installed services
        // if servers not sufficient, create less instances and print warning
        // if servers still not sufficient, print error and exit
        URL url;
        JSONObject stat;
        ServerResources sr;
        String slist;
        
        try
        {
            for (Server srv : servers)
            {
                url = new URL("http://" + srv.getAddress() + ":" + srv.getPort() + "/ServerStatistics/resources/stats");
                stat = new JSONObject(util.callGET(url));
                sr = new ServerResources(srv.getName(), stat);
                // check if server fits the criteria (heap mem usage < 80%, space free > 1 GB, cpu usage < 80 %)
                if (sr.areResourcesAvailable())
                {
                    slist = listServices("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd());
                    sr.addDeployedServices(slist);
                    resources.add(sr);
                }
                else
                {
                    System.out.println("Server " + srv.getName() + " has limited resources and will not be used");
                }
            }
        }
        catch (IOException | JSONException e)
        {
            System.err.println("Exception at deployTopology: " + e);
        }
        
        Map<String, Double> sortedOnCpu = new HashMap<>();
        for (TopologyGraphNode nod : nodes)
        {
            WarFile wfile = kv.getWarFile(nod.getName());
            int instances = nod.getInstances();
            
            // check if service not already deployed
            // ensure that instances < num of available servers
            
            sortedOnCpu.clear();
            for (ServerResources sres : resources)
            {
                if (sres.isServiceDeployed(wfile.getComponent_id())) continue;
                sortedOnCpu.put(sres.getServername(), sres.getSystemCpuLoad());
            }
            
            if (sortedOnCpu.size() < 1)
            {
                System.err.println("There are no servers for deploying, aborting...");
                return null; // <----------------------------------------------------------------------------------- Vorsicht !!! ------------
            }
            
            if (instances > sortedOnCpu.size())
            {
                System.out.println("There are not enough servers for deploying " + instances + " instances of service " + nod.getName());
                System.out.println("Setting instances = " + sortedOnCpu.size());
                instances = sortedOnCpu.size();
                nod.setInstances(instances);
            }
            
            // take the most appropriate server(s) (sort remaining based on cpu usage)
            sortedOnCpu = MapUtil.sortByValueAscending(sortedOnCpu);
            Iterator<Map.Entry<String, Double>> iter = sortedOnCpu.entrySet().iterator();
            
            // now we want to deploy 'instances' instances of WarFile 'wfile' to the remaining servers
            int num_deployed = 0;
            while (iter.hasNext() && num_deployed < instances) 
            {
                Server srv = kv.getServer(iter.next().getKey());
                
                
            }
            
            // open war file
            // include kv host,port topology id, instance id
            // deploy services according to configuration
            // on error, print and exit
            // on success, set stati to 'installed'
        }
        
        // create a configuration json
        JSONObject server_conf = new JSONObject();
        
        
        
        // save configuration to the topology
        topo.setServer_config(server_conf);
        
        // save topology to the kv
        kv.putTopology(topo);
                
        return null;
    }
    
    public String startTopology(String topo_name)
    {
        // get topology json from kv
        Topology topo = kv.getTopology(topo_name);
        
        // get servers from kv
        ArrayList<Server> servers = kv.getServers();
        
        // get the configuration json
        JSONObject server_conf = topo.getServer_config();
        try
        {
            // run services
            util.callGET(new URL(""));
            // on error, print and exit
            // on success, set stati to 'running'
        }
        catch (IOException ex)
        {
            Logger.getLogger(Deployer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // save configuration to the topology
        topo.setServer_config(server_conf);
        
        // save topology to the kv
        kv.putTopology(topo);
                
        return null;
    }
    
    public String stopTopology(String topo_name)
    {        
        // get topology json from kv
        Topology topo = kv.getTopology(topo_name);
                
        // get servers from kv
        ArrayList<Server> servers = kv.getServers();
        
        // get server configuration
        JSONObject server_conf = topo.getServer_config();
        
        // for each server, call stop
        // on error, print warning
        // on success, set stati to 'installed'
        
        // save configuration to the topology
        topo.setServer_config(server_conf);
        
        // save topology to the kv
        kv.putTopology(topo);
                
        return null;
    }
    
    public String undeployTopology(String topo_name)
    {        
        // get topology json from kv
        Topology topo = kv.getTopology(topo_name);
                
        // get servers from kv
        ArrayList<Server> servers = kv.getServers();
        
        // get server configuration
        JSONObject server_conf = topo.getServer_config();
        
        // for each server, call undeploy
        // on error, print warning
        // on success, delete item from config
        
        // if everything ok, server_conf should be empty
        
        // save configuration to the topology
        topo.setServer_config(null);
        
        // save topology to the kv
        kv.putTopology(topo);
                
        return null;
    }
    
    public String deployService(String httpserverandport, String servercredentials, String warfilepath, String webservicepath)
    {
        // httpserverandport: server to call, e.g. "http://localhost:8080"
        // servercredentials: server credentials, e.g. "admin:passwd"
        // warfilepath: path to the WAR file
        // webservicepath: service path, e.g. "/SEIndexShardService"
        
        String output;
            
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
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during web service deployment: " + e;
        }
        
        return output;
    }
    
    public String undeployService(String httpserverandport, String servercredentials, String webservicepath)
    {
        String output;
        
        try
        {
            URL url = new URL(httpserverandport + "/manager/text/undeploy?path=" + webservicepath);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            //httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
            httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during web service undeployment: " + e;
        }
        
        return output;
    }
    
    public String listServices(String httpserverandport, String servercredentials)
    {
        String output;

        try
        {
            URL url = new URL(httpserverandport + "/manager/text/list");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during web service listing: " + e;
        }
        
        return output;
    }
    
    public String serverStatus(String httpserverandport, String servercredentials)
    {
        String output;
        
        try
        {
            URL url = new URL(httpserverandport + "/manager/status");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            output = "IOException during receiving server status: " + e;
        }
        
        return output;
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
