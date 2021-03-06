package gr.iti.openzoo.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gr.iti.openzoo.admin.Blackboard;
import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.admin.ServiceParameters;
import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public abstract class OpenZooWorker implements Runnable {

    protected static Logger log = LogManager.getLogger(OpenZooWorker.class.getName());
    private static ConnectionFactory factory = new ConnectionFactory();
    
    private Thread runner;
    protected Boolean enough = false;
    protected String thread_name = null;
    
    private Connection connection;
    protected Channel channel;
    
    protected ServiceParameters serviceParams = null;
    protected Blackboard kv;
    
    protected HashMap<String, JSONObject> requests = new HashMap<>();
    
    public OpenZooWorker(String name)
    {
        log.debug("-- OpenZooWorker()");
        
        this.thread_name = name;
    }
    
    public abstract boolean doWork(Message message);
    public abstract String publish(JSONObject obj);

    @Override
    public abstract void run();

    public void startIt()
    {
        log.debug("-- OpenZooWorker.startIt");
        
        kv = new Blackboard(serviceParams.getKV().getHost(),
                serviceParams.getKV().getPort(),
                serviceParams.getKV().getUser(),
                serviceParams.getKV().getPasswd(),
                serviceParams.getKV().getDb());
                
        factory.setHost(serviceParams.getRabbit().getHost());
        factory.setPort(serviceParams.getRabbit().getPort());
        String usr = serviceParams.getRabbit().getUser();
        String pwd = serviceParams.getRabbit().getPasswd();
        if (usr != null && !usr.isEmpty())
        {
            factory.setUsername(usr);
            factory.setPassword(pwd);
        }
        
        try
        {
            connection = factory.newConnection();
            channel = connection.createChannel();     
            channel.basicQos(1);
        }
        catch (IOException ex) 
        {
            log.error("IOException during connecting to rabbitmq: " + ex);
        }
        
        runner = new Thread(this, thread_name);
	runner.start();
    }
    
    public void stopIt()
    {
        log.debug("-- OpenZooWorker.stopIt");
        
        enough = true;
        runner.interrupt(); // -M-
        
        if (connection != null)
            try
            {
                connection.close();
            }
            catch (IOException ex) 
            {
                log.error("IOException during closing connections to rabbitmq: " + ex);
            }
        
        kv.stop();
    }
        
    protected void setServiceParameters(ServiceParameters spv2)
    {
        log.debug("-- OpenZooWorker.setServiceParameters");
        
        serviceParams = spv2;
    }
        
    // TODO: add instance id for giving  different parameters to different instances
    public String getRequiredParameter(String param)
    {
        //return kv.getHashValue("topologies:" + serviceParams.getGeneral().getTopologyID(), "requires:" + serviceParams.getGeneral().getComponentID() + ":" + param);
        return kv.getRequiredParameter(serviceParams.getGeneral().getTopologyID(), serviceParams.getGeneral().getComponentID(), param);
    }
    
    protected Message createEmptyMessage()
    {
        return new Message( new JSONObject(), 
                            new JSONObject(), 
                            serviceParams.getGeneral().getComponentID(), 
                            serviceParams.getGeneral().getInstanceID(), 
                            new Throwable().getStackTrace()[1].getClassName().toString(), 
                            null);
    }
    
    public String putRequest(Message msg)
    {
        String hash = msg.getID();
        requests.put(hash, null);
        return hash;
    }
    
    public JSONObject getResponse(String hash, int timeout)
    {
        JSONObject response;
        int attempts = 0;
        while (true)
        {
            response = requests.get(hash);
            
            if (response != null || attempts++ >= timeout) break;
            
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                log.info("No more sleep");
            }
        }
        
        requests.remove(hash);
        
        return response;
    }
    
    protected void clearRequests()
    {
        requests.clear();
    }
    
    protected HashMap<String, JSONObject> getRequests()
    {
        return requests;
    }
}
