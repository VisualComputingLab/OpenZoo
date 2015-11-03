package gr.iti.openzoo.impl;

import com.rabbitmq.client.Connection;
import gr.iti.openzoo.admin.KeyValueCommunication;
import gr.iti.openzoo.admin.ServiceParameters;
import gr.iti.openzoo.util.SerializationUtil;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public abstract class OpenZooService {

    protected static final Logger log = LogManager.getLogger(OpenZooService.class.getName());
    
    protected ServiceParameters parameters = new ServiceParameters();
    protected JSONObject properties;
    protected static List<Connection> allConnections;
    protected KeyValueCommunication kv;
    protected ArrayList<OpenZooWorker> workerUnion;
    protected HashSet<String> workerClasses;
    protected String realPath;
        
    public OpenZooService(String appName)
    {                
        log.debug("-- OpenZooService()");

        if ((new File("./webapps/")).exists())
            realPath = (new File("./webapps/" + appName)).getAbsolutePath();    // linux
        else realPath = (new File("../webapps/" + appName)).getAbsolutePath();  // windows
        
        try 
        {
            String paramsFile = realPath + "/parameters.ser";
            log.debug("Checking for parameter file in " + paramsFile);
            parameters = (ServiceParameters) SerializationUtil.deserialize(paramsFile);
        } 
        catch (IOException e) 
        {
            log.error("Error deserializing Service Params " + e);
        } 
        catch (ClassNotFoundException e) 
        {
            log.error("Error class not found Service Params " + e);
        }
        
        kv = new KeyValueCommunication(parameters.getRedis().getHost(), parameters.getRedis().getPort());
        
        
        readParametersFromKV();
                
        // worker and endpoint parameters already there
        
        workerUnion = new ArrayList<>();
        workerClasses = new HashSet<>();
    }
        
    final public void readParametersFromKV()
    {
        //read from KV
            
        // general
        parameters.getGeneral().setNumOfThreadsPerCore(0); // default special value, means that there should be only one thread, irrelevant of num of processors
        String node_object = kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "node:" + parameters.getGeneral().getComponentID());
        try
        {
            JSONObject node_json = new JSONObject(node_object);
            parameters.getGeneral().setNumOfThreadsPerCore(node_json.getInt("threadspercore"));
        }
        catch (JSONException e)
        {
            log.error("JSONException while trying to read threadspercore parameter from KV: " + e);
        }
        
        
        

        // rabbit
        parameters.getRabbit().setHost(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "rabbit:host"));
        parameters.getRabbit().setPort(Integer.parseInt(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "rabbit:port")));
        parameters.getRabbit().setUser(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "rabbit:user"));
        parameters.getRabbit().setPasswd(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "rabbit:passwd"));
        parameters.getRabbit().setVhost(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "rabbit:vhost"));

        // redis is already there

        // mongo      
        parameters.getMongo().setHost(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "mongo:host"));
        parameters.getMongo().setPort(Integer.parseInt(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "mongo:port")));
        parameters.getMongo().setUser(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "mongo:user"));
        parameters.getMongo().setPasswd(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "mongo:passwd"));
        parameters.getMongo().setDb(kv.getHashValue("topologies:" + parameters.getGeneral().getTopologyID(), "mongo:db"));
    }
    
    public JSONObject startWorkers(String className)
    {
        log.debug("-- OpenZooService.startWorkers " + className);
        
        int cores = Runtime.getRuntime().availableProcessors();
        int numOfThreadsPerCore = parameters.getGeneral().getNumOfThreadsPerCore();
        int numThreads;
        
        if (numOfThreadsPerCore == 0)
        {
            numThreads = 1;
        }
        else
        {
            if (cores < 1)
            {
                log.debug("Number of cores reported = " + cores);
                cores = 1;
            }
            else
            {
                log.debug("Number of cores available = " + cores);
            }
            
            numThreads = cores*numOfThreadsPerCore;
        }

        log.info("I'll start " + numThreads + " threads");

        JSONObject response;
        
        try
        {
            response = new JSONObject();
            JSONArray consumerArray = new JSONArray();
                
            try
            {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getConstructor(String.class);
                
                String threadName;
                int i;

                for (i = 0; i < numThreads; i++)
                {
                    threadName = "consumer_" + i;
                    
                    OpenZooWorker consumer = (OpenZooWorker) ctor.newInstance(threadName);
                    
                    consumer.setServiceParameters(parameters);
                    workerUnion.add(consumer);
                    workerClasses.add(className);
                    consumer.startIt();
                    consumerArray.put(threadName);
                }

                response.put("message", "Created " + i + " threads");
                response.put("threads", consumerArray);
            }
            catch (ReflectiveOperationException ex) 
            {
                log.error("ReflectiveOperationException in startWorkers: " + ex);
                response.put("error", ex);
                ex.printStackTrace();
            } 
            catch (IllegalArgumentException ex) 
            {
                log.error("IllegalArgumentException in startWorkers: " + ex);
                response.put("error", ex);
            }
        }
        catch (JSONException ex) 
        {
            log.error("JSONException in startWorkers: " + ex);
            return null;
        }
        
        return response;
    }
    
    public JSONObject stopWorkers()
    {
        log.debug("-- OpenZooService.stopWorkers");
        int i = 0;
        JSONObject response;
        
        try
        {
            response = new JSONObject();
            for (OpenZooWorker ozw : workerUnion)
            {
                ozw.stopIt();
                i++;
            }
            response.put("message", "Destroyed " + i + " threads");
        
            workerUnion.clear();
            workerClasses.clear();
        }
        catch (JSONException ex) 
        {
            log.error("JSONException in stopWorkers: " + ex);
            return null;
        }
        
        return response;
    }
         
    public JSONObject reset()
    {
        log.debug("-- OpenZooService.reset");
        
        JSONObject response;
        
        try
        {
            response = new JSONObject();
            
            // backup workerClasses
            HashSet<String> wccopy = (HashSet) workerClasses.clone();
            
            // stop workers
            JSONObject stopjson = stopWorkers();
            response.put("stop", stopjson);

            // update parameters from KV
            kv.stop();
            
            try 
            {
                String paramsFile = realPath + "/parameters.ser";
                log.debug("Checking for parameter file in " + paramsFile);
                parameters = (ServiceParameters) SerializationUtil.deserialize(paramsFile);
            } 
            catch (IOException e) 
            {
                log.error("Error deserializing Service Params " + e);
            } 
            catch (ClassNotFoundException e) 
            {
                log.error("Error class not found Service Params " + e);
            }

            kv = new KeyValueCommunication(parameters.getRedis().getHost(), parameters.getRedis().getPort());
            readParametersFromKV();
            log.debug(parameters.toString());
            

            // restart worker with new parameters
            for (String cl : wccopy)
            {
                JSONObject startjson = startWorkers(cl);
                response.put("start", startjson);
            }
            wccopy.clear();
        }
        catch (JSONException ex) 
        {
            log.error("JSONException in reset: " + ex);
            return null;
        }
        
        return response;
    }
    
    public JSONObject status()
    {
        log.debug("-- OpenZooService.statusWorkers");
        JSONObject response;
        
        try
        {
            response = new JSONObject();
            response.put("message", "" + workerUnion.size() + " threads active");
        }
        catch (JSONException ex) 
        {
            log.error("JSONException in statusWorkers: " + ex);
            return null;
        }
        
        return response;
    }
}
