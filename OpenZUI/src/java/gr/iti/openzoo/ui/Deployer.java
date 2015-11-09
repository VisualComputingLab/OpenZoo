package gr.iti.openzoo.ui;

import gr.iti.openzoo.pojos.TopologyGraphNode;
import gr.iti.openzoo.pojos.WarFile;
import gr.iti.openzoo.pojos.Triple;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.pojos.Server;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.bind.DatatypeConverter;
import org.codehaus.jettison.json.JSONArray;
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
    
    private static final int THREADPOOL_SIZE = 5;
    
    public Deployer(JSONObject properties, KeyValueCommunication thekv)
    {
        try 
        {
            kv = thekv;
            repo = properties.getString("localRepository");
        }
        catch (JSONException ex) 
        {
            System.err.println("ERROR retrieving keyValue server: " + ex);
        }
    }
    
    public List<String> produceServerConfiguration(Topology topo, ArrayList<JSONObject> triples)
    {
        List<String> logs = Collections.synchronizedList(new ArrayList<String>());
        
        // get topology and servers from kv
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
                url = new URL("http://" + srv.getAddress() + ":" + srv.getPort() + "/ServerResources/resources/manage");
                stat = new JSONObject(util.callGET(url, null, null));
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
            sortedResources.add(entry.getValue());
        }
        
        // Cycle now through sortedResources and throw a service instance at each server
        int res_index = 0;
        int assigned;
        
        JSONObject oneResult;
        JSONObject server_conf;
        
        for (TopologyGraphNode nod : nodes)
        {            
            WarFile wfile = kv.getWarFile(nod.getName());
                        
            int instances = nod.getInstances();
            assigned = 0;
            ServerResources sres;
            
            // check if service not already deployed
            // ensure that instances < num of available servers
            
            for (int i = 0; i < sortedResources.size() && assigned < instances; i++)
            {
                sres = sortedResources.get((res_index) % sortedResources.size());
                res_index++;
                
                if (sres.isServiceDeployed(wfile.getComponent_id()))
                    continue;
                try
                {
                    server_conf = new JSONObject().put("instance_id", assigned).put("threadspercore", nod.getThreadspercore()).put("status", "void");
                    oneResult = new JSONObject();
                    oneResult.put("server_id", sres.getServername());
                    oneResult.put("component_id", wfile.getComponent_id());
                    oneResult.put("component_type", wfile.getType());
                    oneResult.put("server_conf", server_conf);
                    triples.add(oneResult);
                }
                catch (JSONException ex)
                {
                    System.err.println("JSONException while creating triple: " + ex);
                    logs.add("ERROR:" + "JSONException while creating triple: " + ex);
                }

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
        
        return logs;
    }
    
    public List<String> deployTopologyServices(Topology topo, JSONArray triples)
    {
        List<String> logs = Collections.synchronizedList(new ArrayList<String>());        
        
        if (triples == null) return logs;
        
        JSONObject server_conf;
        
        WarFile war;
        String servername, war_id, inst_id;
        
        // create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
        int index = 0;
        int tpc;
        
        ArrayList<JSONObject> allData = new ArrayList<>();
        
        ArrayList<TopologyGraphNode> nodes = topo.getNodes();
        
        // split jobs
        for (int i = 0; i < triples.length(); i++)
        {
            JSONObject triplo = triples.optJSONObject(i);
            if (triplo == null) continue;
            servername = triplo.optString("server_id");
            war_id = triplo.optString("component_id");
            inst_id = triplo.optString("instance_id");
            war = (WarFile) kv.getWarFile(war_id);
            tpc = 0;
            for (TopologyGraphNode nod : nodes)
                if (nod.getName().equalsIgnoreCase(war_id))
                {
                    tpc = nod.getThreadspercore();
                    break;
                }
            
            try
            {
                server_conf = new JSONObject().put("instance_id", inst_id).put("threadspercore", tpc).put("status", "void");
            }
            catch (JSONException ex)
            {
                System.err.println("JSONException while creating server conf: " + ex);
                logs.add("ERROR:" + "JSONException while creating server conf: " + ex);
                continue;
            }
            
            allData.add(new JSONObject());
            Runnable worker = new WorkerThread("deploy", servername, war, server_conf, repo, allData.get(index), index, topo.getName(), kv, logs);
            index++;
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated())
        {
        }
         
        // merge results
        JSONObject conf_object = new JSONObject();
        Iterator<String> iter, iter2;
        String s_comp, s_comp2;
        JSONObject j_comp, j_comp2;
        
        for (JSONObject json : allData)
        {
            iter = json.keys();
            while (iter.hasNext())
            {
                try
                {
                    s_comp = iter.next();
                    j_comp = json.getJSONObject(s_comp);
                    if (conf_object.has(s_comp))
                    {
                        iter2 = j_comp.keys();
                        while (iter2.hasNext())
                        {
                            s_comp2 = iter2.next();
                            j_comp2 = j_comp.getJSONObject(s_comp2);
                            conf_object.getJSONObject(s_comp).put(s_comp2, j_comp2);
                        }
                    }
                    else
                    {
                        conf_object.put(s_comp, j_comp);
                    }
                }
                catch (JSONException e)
                {
                    System.err.println("JSONException during merging deployment results: " + e);
                    logs.add("WARN:" + "JSONException during merging deployment results: " + e);
                }
            }
        }
            
                
        // save configuration to the topology
        topo.setConf_object(conf_object);
        
        // save topology to the kv
        // update topo.getGraph_object()
        kv.putTopology(topo);
                
        return logs;
    }
    
    public List<String> undeployTopology(String topo_name)
    {        
//        ArrayList<String> logs = new ArrayList<>();
        List<String> logs = Collections.synchronizedList(new ArrayList<String>());
        
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
                
                ArrayList<Triple<String, WarFile, JSONObject>> triples = new ArrayList<>();

                // server items to be deleted from each service item
                ArrayList<String> serversToDelete = new ArrayList<>();
                
                WarFile war;

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
                        war = kv.getWarFile(service_id);
                        triples.add(new Triple(server_id, war, server_json));
                    }
                }
                
                // create thread pool
                ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
                ArrayList<JSONObject> allData = new ArrayList<>(); // using allData for communicating the results
                int index = 0;
                
                // split jobs
                for (Triple triplo : triples)
                {
                    server_id = (String) triplo.getLeft();
                    war = (WarFile) triplo.getMiddle();
                    server_json = (JSONObject) triplo.getRight();
                    allData.add(new JSONObject());
                    Runnable worker = new WorkerThread("undeploy", server_id, war, server_json, repo, allData.get(index), index, topo_name, kv, logs);
                    index++;
                    executor.execute(worker);
                }
                executor.shutdown();
                while (!executor.isTerminated())
                {
                }

                // merge results
                Iterator<String> iter;
                String s_comp, s_serv;
                JSONObject j_comp;

                for (JSONObject json : allData)
                {
                    iter = json.keys();
                    while (iter.hasNext())
                    {
                        try
                        {
                            s_comp = iter.next();
                            s_serv = json.getString(s_comp);
                            j_comp = conf_object.getJSONObject(s_comp);
                            if (j_comp != null)
                            {
                                j_comp.remove(s_serv);
                                if (j_comp.length() == 0) conf_object.remove(s_comp);
                            }
                        }
                        catch (JSONException e)
                        {
                            System.err.println("JSONException during merging undeployment results: " + e);
                            logs.add("WARN:" + "JSONException during merging undeployment results: " + e);
                        }
                    }
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
    
    public List<String> startTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "start");
    }
    
    public List<String> stopTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "stop");
    }
    
    public List<String> statusTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "status");
    }
    
    public List<String> resetTopology(String topo_name)
    {
        return callTopologyServices(topo_name, "reset");
    }
    
    public List<String> callTopologyServices(String topo_name, String command)
    {
//        ArrayList<String> logs = new ArrayList<>();
        List<String> logs = Collections.synchronizedList(new ArrayList<String>());
                
        // get topology json from kv
        Topology topo = kv.getTopology(topo_name);
                
        // get the configuration json
        JSONObject conf_object = topo.getConf_object();
        
        try
        {
            Iterator it_service = conf_object.keys();
            String service_id, server_id;
            JSONObject service_json, server_json;
            ArrayList<Triple<String, WarFile, JSONObject>> triples = new ArrayList<>();
            WarFile war;
            
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
                    war = kv.getWarFile(service_id);
                    triples.add(new Triple(server_id, war, server_json));
                }
            }
            
            // create thread pool
            ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
            ArrayList<JSONObject> allData = new ArrayList<>();
            int index = 0;

            // split jobs
            for (Triple triplo : triples)
            {
                server_id = (String) triplo.getLeft();
                war = (WarFile) triplo.getMiddle();
                server_json = (JSONObject) triplo.getRight();
                allData.add(new JSONObject());
                Runnable worker = new WorkerThread(command, server_id, war, server_json, repo, allData.get(index), index, topo_name, kv, logs);
                index++;
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated())
            {
            }
        }
        catch (JSONException ex)
        {
            System.err.println("JSONException in callTopologyServices: " + ex);
            logs.add("ERROR:" + "JSONException in callTopologyServices: " + ex);
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
    
    public List<String> updateService(String topo_name, String service_id, boolean redeploy)
    {
        //ArrayList<String> logs = new ArrayList<>();
        List<String> logs = Collections.synchronizedList(new ArrayList<String>());
        
        // get topology and servers from kv
        Topology topo = kv.getTopology(topo_name);
        JSONObject conf_object = topo.getConf_object();
        JSONObject service_json = conf_object.optJSONObject(service_id);
        
        if (service_json == null)
        {
            System.err.println("Could not find service " + service_id + " in conf_object");
            logs.add("ERROR:" + "Could not find service " + service_id + " in conf_object");
            return logs;
        }
        
        Iterator<String> iter = service_json.keys();
        String server_id;
        JSONObject server_json;
        HashMap<String, JSONObject> server2conf = new HashMap<>();
        
        while (iter.hasNext())
        {
            server_id = iter.next();
            server_json = service_json.optJSONObject(server_id);
            if (server_json != null)
            {
                server2conf.put(server_id, server_json);
            }
        }
        
        WarFile war = kv.getWarFile(service_id);
        
        // create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
        int index = 0;
                
        // split jobs
        for (String servername : server2conf.keySet())
        {
            server_json = server2conf.get(servername);
            Runnable worker;
            if (redeploy)
                worker = new WorkerThread("redeploy", servername, war, server_json, repo, null, index, topo_name, kv, logs);
            else worker = new WorkerThread("reset", servername, war, server_json, repo, null, index, topo_name, kv, logs);
            index++;
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated())
        {
        }
        
        // check if needed to put server_json back into the conf_object
        
        // save configuration to the topology
        topo.setConf_object(conf_object);
        
        // save topology to the kv
        // update topo.getGraph_object()
        kv.putTopology(topo);
                
        return logs;
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
