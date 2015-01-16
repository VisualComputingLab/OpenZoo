package gr.iti.openzoo.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import gr.iti.openzoo.admin.KeyValueCommunication;
import gr.iti.openzoo.admin.ServiceParameters;
import gr.iti.openzoo.util.SerializationUtil;
import gr.iti.openzoo.util.Utilities;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
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
 * @author Dimitris Samaras <dimitris.samaras@iti.gr>
 */
public abstract class OpenZooService {

    protected static final Logger log = LogManager.getLogger(OpenZooService.class.getName());
    
    protected String service_name;
    private Utilities util = new Utilities();
    private String myip = util.getHostname();
    private String serverHostname = util.getHostname();
    protected ServiceParameters parameters = new ServiceParameters();
    protected JSONObject properties;
    protected ConnectionFactory factory;
    protected Connection connectionOUT;
    protected Channel channelOUT;
    protected Connection connectionIN;
    protected Channel channelIN;
    protected QueueingConsumer qconsumer1;
    protected ServiceThread service1;
    protected static List<Connection> allConnections;
    protected static List<ServiceThread> allServices;
    protected int parallelismIdx = 1;
    protected KeyValueCommunication kv;
    protected ArrayList<OpenZooWorker> workerUnion;
    protected HashSet<String> workerClasses;
    private String realPath;
        
    /**
     * The class constructor
     * <br />
     * Retrieve values from the KV app folder about complementary services
     * installed on the Consul
     *
     * @param jsonObject JSONObject, the Rest post call payload
     *
     */
    public OpenZooService()
    {
        //Configurator.initialize(OpenZooService.class.getName(), ".");
        
        log.debug("-- OpenZooService()");

        realPath = System.getProperty("ApplicationPath");
        
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
        
        service_name = parameters.getGeneral().getName();
        kv = new KeyValueCommunication(parameters.getRedis().getHost(), parameters.getRedis().getPort());
        
        
        readParametersFromKV();
        

        //JSONObject json = new JSONObject(kv.getValue("queue"));
        
        // worker and endpoint parameters already there
        
        log.debug(parameters.toString());

        
        workerUnion = new ArrayList<>();
        workerClasses = new HashSet<>();
    }
    
    final public void readParametersFromKV()
    {
        //read from KV
            
        // general
        parameters.getGeneral().setNumOfWorkersPerCore(Integer.parseInt(kv.getValue(parameters.getGeneral().getComponentID() + ":numOfWorkersPerCore")));

        // rabbit
        parameters.getRabbit().setHost(kv.getValue("rabbitmq:host"));
        parameters.getRabbit().setPort(Integer.parseInt(kv.getValue("rabbitmq:port")));
        parameters.getRabbit().setUser(kv.getValue("rabbitmq:user"));
        parameters.getRabbit().setPasswd(kv.getValue("rabbitmq:passwd"));
        parameters.getRabbit().setVhost(kv.getValue("rabbitmq:vhost"));

        // redis is already there

        // mongo      
        parameters.getMongo().setHost(kv.getValue("mongodb:host"));
        parameters.getMongo().setPort(Integer.parseInt(kv.getValue("mongodb:port")));
        parameters.getMongo().setUser(kv.getValue("mongodb:user"));
        parameters.getMongo().setPasswd(kv.getValue("mongodb:passwd"));
        parameters.getMongo().setDb(kv.getValue("mongodb:db"));
    }
    
//    private void addWorker(OpenZooWorker ozw)
//    {
//        log.debug("-- OpenZooService.addWorker");
//        ozw.setServiceParameters(parameters);
//        workerUnion.add(ozw);
//        ozw.startIt();
//    }
    
    public JSONObject startWorkers(String className)
    {
        log.debug("-- OpenZooService.startWorkers " + className);
        
        int cores = Runtime.getRuntime().availableProcessors();
        int numOfWorkersPerCore = parameters.getGeneral().getNumOfWorkersPerCore();
                    
        if (cores < 1)
        {
            log.debug("Number of cores reported = " + cores);
            cores = 1;
        }
        else
        {
            log.debug("Number of cores available = " + cores);
        }
        
        int numWorkers = cores*numOfWorkersPerCore;

        log.info("I'll start " + numWorkers + " workers");

        JSONObject response;
        
        try
        {
            response = new JSONObject();
            JSONArray consumerArray = new JSONArray();
                
            try
            {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getConstructor(String.class);
                //Object[] params = new Object[] {"test consumer xx"};
                
                String threadName;
                int i;

                for (i = 0; i < numWorkers; i++)
                {
                    threadName = "consumer_" + i;
                    OpenZooWorker consumer = (OpenZooWorker) ctor.newInstance(threadName);
//                    addWorker(consumer);
                    consumer.setServiceParameters(parameters);
                    workerUnion.add(consumer);
                    workerClasses.add(className);
                    consumer.startIt();
                    consumerArray.put(threadName);
                }

                response.put("message", "Created " + i + " workers");
                response.put("workers", consumerArray);
            }
            catch (ReflectiveOperationException ex) 
            {
                log.error("ReflectiveOperationException in startWorkers: " + ex);
                response.put("error", ex);
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
            response.put("message", "Destroyed " + i + " workers");
        
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

            service_name = parameters.getGeneral().getName();
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
        int i = 0;
        JSONObject response;
        
        try
        {
            response = new JSONObject();
            response.put("message", "" + workerUnion.size() + " workers active");
        }
        catch (JSONException ex) 
        {
            log.error("JSONException in statusWorkers: " + ex);
            return null;
        }
        
        return response;
    }
}
