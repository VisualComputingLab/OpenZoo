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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    private static String kv_host;
    private static Integer kv_port;
    
    public Deployer(JSONObject properties, KeyValueCommunication thekv)
    {
        try 
        {
            kv_host = properties.getJSONObject("keyvalue").getString("host");
            kv_port = properties.getJSONObject("keyvalue").getInt("port");
            kv = thekv;
            repo = properties.getString("localRepository");
        }
        catch (JSONException ex) 
        {
            System.err.println("ERROR retrieving keyValue server: " + ex);
        }
    }
    
    public ArrayList<String> deployTopology(String topo_name)
    {
        ArrayList<String> logs = new ArrayList<>();
        
        // get topology and servers from kv
        Topology topo = kv.getTopology(topo_name);
        ArrayList<Server> servers = kv.getServers();
        
        ArrayList<TopologyGraphNode> nodes = topo.getNodes();
        Map<String, ServerResources> server2resources = new HashMap<>();
        
        // for each service instance, choose a server based on server statistics and previously installed services
        // if servers not sufficient, create less instances and print warning
        // if servers still not sufficient, print error and exit
        URL url;
        JSONObject stat;
        ServerResources sr;
        ArrayList<String> slist;
        
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
                    server2resources.put(sr.getServername(), sr);
//                    System.out.println("Server " + srv.getName() + " will be used");
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
            logs.add("ERROR:" + "Exception at deployTopology: " + e);
            return logs;
        }
        
        server2resources = MapUtil.sortByValueAscending( server2resources );
                
        ArrayList<ServerResources> sortedResources = new ArrayList<>();
        for(Map.Entry<String, ServerResources> entry : server2resources.entrySet()) {
//            System.out.println("Resource " + entry.getValue().getServername() + ": " + entry.getValue().getSystemCpuLoad());
            sortedResources.add(entry.getValue());
        }
        
//        System.out.println("Size of sortedResources is " + sortedResources.size());
        
        // Cycle now through sortedResources and throw a service instance at each server
        int res_index = 0;
        int assigned;
        ArrayList<Triple<String, WarFile, JSONObject>> triples = new ArrayList<>();
        JSONObject server_conf;
        
        for (TopologyGraphNode nod : nodes)
        {            
            WarFile wfile = kv.getWarFile(nod.getName());
            
//            System.out.println("Checking node " + nod.getName() + "(" + wfile.getComponent_id() + ")");
            
            int instances = nod.getInstances();
            assigned = 0;
            ServerResources sres;
            
            // check if service not already deployed
            // ensure that instances < num of available servers
            
            for (int i = 0; i < sortedResources.size() && assigned < instances; i++)
            {
                sres = sortedResources.get((res_index) % sortedResources.size());
//                System.out.println("Checking resource " + sres.getServername());
                res_index++;
                
                if (sres.isServiceDeployed(wfile.getComponent_id()))
                    continue;
                try
                {
                    server_conf = new JSONObject().put("instance_id", assigned).put("threadspercore", nod.getThreadspercore()).put("status", "void");
                    triples.add(new Triple(sres.getServername(), wfile, server_conf));
                }
                catch (JSONException ex)
                {
                    System.err.println("JSONException while creating triple: " + ex);
                    logs.add("ERROR:" + "JSONException while creating triple: " + ex);
                }
//                System.out.println("Triple added");
                assigned++;
            }
            if (assigned == 0)
            {
                System.err.println("There are no servers for deploying service " + nod.getName() + ", aborting...");
                logs.add("ERROR:" + "There are no servers for deploying service " + nod.getName() + ", aborting...");
                return logs;
            }
            else if (assigned < instances)
            {
                System.out.println("There are not enough servers for deploying " + instances + " instances of service " + nod.getName() + ". Instance number is set to " + assigned);
                logs.add("WARN:" + "There are not enough servers for deploying " + instances + " instances of service " + nod.getName() + ". Instance number is set to " + assigned);
                nod.setInstances(instances);
            }
        }
        
        // create a configuration json
        JSONObject conf_object = new JSONObject();
        JSONObject service_conf;
        
        WarFile war;
        String servername;
        
        for (Triple triplo : triples)
        {
//            System.out.println(triplo.toString());
            
            servername = (String) triplo.getLeft();
            war = (WarFile) triplo.getMiddle();
            server_conf = (JSONObject) triplo.getRight();
            
            // open war file
            // include kv_host, kv_port, topo_name, instance_id into config.json
            File f_copy;
            JSONObject config = Utilities.readJSONFromWAR(repo + "/" + war.getFilename(), "config.json");
            try
            {
                config.put("keyvalue", new JSONObject().put("host", kv_host).put("port", kv_port));
                config.put("instance_id", server_conf.getInt("instance_id"));
                config.put("topology_id", topo_name);
                
                File f = new File(repo + "/" + war.getFilename());
                f_copy = new File(repo + "/" + war.getFilename() + "_" + servername + ".war");
                Files.copy(f.toPath(), f_copy.toPath(), StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Updating config.json in " + f_copy.getAbsolutePath());
                Utilities.writeJSONToWAR(f_copy.getAbsolutePath(), "config.json", config);
                
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
                System.out.println("Exception during writing to config.json: " + e);
                logs.add("ERROR:" + "Exception during writing to config.json: " + e);
                return logs;
            }
                        
            
            // deploy services according to configuration
            Server srv = kv.getServer(servername);
            JSONObject outjson = deployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), f_copy.getAbsolutePath(), "/" + war.getComponent_id());
            
            // on error, print and go to next
            // on success, set stati to 'installed'
            try
            {
                if (outjson.getString("status").equalsIgnoreCase("success"))
                {
                    conf_object.getJSONObject(war.getComponent_id()).getJSONObject(servername).put("status", "installed");
                }
                else
                {
                    System.err.println("Service deployment failed with: " + outjson.getString("message"));
                    logs.add("ERROR:" + "Service deployment failed with: " + outjson.getString("message"));
                    continue;
                }
            }
            catch (JSONException e)
            {
                System.out.println("Exception during updating conf_object: " + e);
                logs.add("ERROR:" + "Exception during updating conf_object: " + e);
                return logs;
            }
        }      
                
        // save configuration to the topology
        topo.setConf_object(conf_object);
        
        // save topology to the kv
        // update topo.getGraph_object()
        kv.putTopology(topo);
                
        return logs;
    }
    
    public ArrayList<String> startTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "start");
    }
    
    public ArrayList<String> stopTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "stop");
    }
    
    public ArrayList<String> statusTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "status");
    }
    
    public ArrayList<String> resetTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "reset");
    }
    
    public ArrayList<String> callTopologyServices(String topo_name, String command)
    {
        ArrayList<String> logs = new ArrayList<>();
                
        // get topology json from kv
        Topology topo = kv.getTopology(topo_name);
                
        // get the configuration json
        JSONObject conf_object = topo.getConf_object();
        
        try
        {
            Iterator it_service = conf_object.keys();
            String service_id, server_id;
            JSONObject service_json, server_json;
            
            while (it_service.hasNext())
            {
                service_id = (String) it_service.next();
                service_json = conf_object.getJSONObject(service_id);
                Iterator it_server = service_json.keys();
                while (it_server.hasNext())
                {
                    server_id = (String) it_server.next();
                    server_json = service_json.getJSONObject(server_id);
                    
                    // run services
                    Server srv = kv.getServer(server_id);
                    WarFile war = kv.getWarFile(service_id);
                    URL url = new URL("http://" + srv.getAddress() + ":" + srv.getPort() + "/" + war.getComponent_id() + war.getService_path() + "?action=" + command);
                    JSONObject output = new JSONObject(util.callGET(url));
                    
                    // on error, print and exit
                    // on success, set stati to 'running'
                    if (output.has("error"))
                    {
                        System.err.println("Calling " + command + " on service " + service_id + " on server " + server_id + " failed with output: " + output.toString(4));
                        logs.add("ERROR:" + "Calling " + command + " on service " + service_id + " on server " + server_id + " failed with output: " + output.toString(4));
//                        response.put(service_id + "_" + server_id, output);
//                        return null;
                        
                    }
                    else
                    {
                        switch (command)
                        {
                            case "start":
                                server_json.put("status", "running");
                                break;
                                
                            case "stop":
                                server_json.put("status", "installed");
                                break;
                        }
//                        response.put(service_id + "_" + server_id, output);
                        logs.add("INFO:" + "Service " + service_id + " on server " + server_id + ": " + command);
                    }
                }
            }
        }
        catch (JSONException | IOException ex)
        {
            System.err.println("Exception in callTopologyServices: " + ex);
            logs.add("ERROR:" + "Exception in callTopologyServices: " + ex);
        }
        
        // save configuration to the topology
        topo.setConf_object(conf_object);
        
        // save topology to the kv
        kv.putTopology(topo);
                
        return logs;
    }
        
    public ArrayList<String> undeployTopology(String topo_name)
    {        
        ArrayList<String> logs = new ArrayList<>();
        
        // get topology and servers from kv
        Topology topo = kv.getTopology(topo_name);
        JSONObject conf_object = topo.getConf_object();
        
        // for each service/server pair in config, call undeploy
        // on success, set status to void
        // on error, print warning
        if (conf_object != null)
            try
            {
                Iterator it_service = conf_object.keys();
                String service_id, server_id;
                JSONObject service_json, server_json;

                // server items to be deleted from each service item
                ArrayList<String> serversToDelete = new ArrayList<>();
                ArrayList<String> servicesToDelete = new ArrayList<>();

                while (it_service.hasNext())
                {
                    service_id = (String) it_service.next();
                    service_json = conf_object.getJSONObject(service_id);
                    serversToDelete.clear();

                    Iterator it_server = service_json.keys();
                    while (it_server.hasNext())
                    {
                        server_id = (String) it_server.next();
                        server_json = service_json.getJSONObject(server_id);

                        // run services
                        Server srv = kv.getServer(server_id);
                        WarFile war = kv.getWarFile(service_id);
                        JSONObject outjson = undeployService("http://" + srv.getAddress() + ":" + srv.getPort(), srv.getUser() + ":" + srv.getPasswd(), "/" + war.getComponent_id());

                        // on error, print and exit
                        // on success, set stati to 'running'
                        if (outjson.getString("status").equalsIgnoreCase("success"))
                        {
                            server_json.put("status", "void");
                            serversToDelete.add(server_id);
                        }
                        else
                        {
                            System.err.println("Service undeployment failed with: " + outjson.getString("message"));
                            logs.add("ERROR:" + "Service undeployment failed with: " + outjson.getString("message"));
                            continue;
                        }
                    }

                    // delete all server items with status void
                    for (String srid : serversToDelete)
                    {
                        service_json.remove(srid);
                    }

                    if (service_json.length() == 0)
                    {
                        servicesToDelete.add(service_id);
                    }
                }

                // delete all empty service items
                for (String ssid : servicesToDelete)
                {
                    conf_object.remove(ssid);
                }

                // if everything ok, conf_object should be empty
                if (conf_object.length() == 0)
                    conf_object = null;
            }
            catch (JSONException ex)
            {
                System.err.println("JSONException in undeployTopology: " + ex);
                logs.add("ERROR:" + "JSONException in undeployTopology: " + ex);
                return logs;
            }
         
        
        // save configuration to the topology
        topo.setConf_object(conf_object);
        
        // save topology to the kv
        kv.putTopology(topo);
                
        return logs;
    }
    
    public JSONObject deployService(String httpserverandport, String servercredentials, String warfilepath, String webservicepath)
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
    
    public ArrayList<String> listServices(String httpserverandport, String servercredentials)
    {
        String output, sname;
        ArrayList<String> result = new ArrayList<>();
        String[] split;

        try
        {
            URL url = new URL(httpserverandport + "/manager/text/list");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            split = output.split("\n");
            for (String line : split)
            {
                if (line.startsWith("/"))
                {
                    sname = line.substring(1, line.indexOf(":"));
                    if (sname != null && !sname.isEmpty())
                        result.add(sname);
                }
            }
//            output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;
        }
        catch (IOException e)
        {
            System.err.println("IOException during web service listing: " + e);
            return null;
        }
        
        return result;
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
