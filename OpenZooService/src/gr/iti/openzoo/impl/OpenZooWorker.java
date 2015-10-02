package gr.iti.openzoo.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gr.iti.openzoo.admin.KeyValueCommunication;
import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.admin.ServiceParameters;
import java.io.IOException;
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
    protected KeyValueCommunication kv;
    
    public OpenZooWorker(String name)//, String webpath)
    {
        log.debug("-- OpenZooWorker()");
        
        this.thread_name = name;
        //this.webAppPath = webpath;
    }
    
    public abstract boolean doWork(Message message);
//    public abstract String publish(JSONObject obj);
//    public abstract JSONObject get(String hash);
    @Override
    public abstract void run();

    public void startIt()
    {
        log.debug("-- OpenZooWorker.startIt");
        
        kv = new KeyValueCommunication(serviceParams.getRedis().getHost(), serviceParams.getRedis().getPort());
                
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
    }
        
    protected void setServiceParameters(ServiceParameters spv2)
    {
        log.debug("-- OpenZooWorker.setServiceParameters");
        
        serviceParams = spv2;
    }
        
    // TODO: add instance id for giving  different parameters to different instances
    public String getRequiredParameter(String param)
    {
        return kv.getHashValue("topologies:" + serviceParams.getGeneral().getTopologyID(), "requires:" + serviceParams.getGeneral().getComponentID() + ":" + param);
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
}
