package gr.iti.openzoo.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class OpenZooLoggingConnection {
       
    protected static Logger log = LogManager.getLogger(OpenZooLoggingConnection.class.getName());
    
    private static int RABBITMQ_MESSAGE_TTL = 5000;
    
    private static SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd yyyy zzz HH:mm:ss.SSS");
    
    private Channel channel;
    private String queue_name = null;
    private OpenZooWorker worker;
    private String topologyId, componentId, instanceId, workerId;
    
    public OpenZooLoggingConnection(OpenZooWorker ozw)
    {
        worker = ozw;
    }
    
    public boolean init()
    {
        channel = worker.channel;
                
        // get topology_id, component_id, name, worker_id
        topologyId = worker.serviceParams.getGeneral().getTopologyID();
        componentId = worker.serviceParams.getGeneral().getComponentID();
        instanceId = worker.serviceParams.getGeneral().getInstanceID();
        workerId = new Throwable().getStackTrace()[1].getClassName().toString();                
        
        queue_name = topologyId + "_logging";
        
        try
        {
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", RABBITMQ_MESSAGE_TTL);
            channel.queueDeclare(queue_name, true, false, false, args);
        }
        catch (IOException e)
        {
            log.error("IOException in constructor: " + e);
            return false;
        }
        
        return true;
    }
    
    public void info(String message)
    {
        put(message, "info");
    }
    
    public void error(String message)
    {
        put(message, "error");
    }
    
    public void debug(String message)
    {
        put(message, "debug");
    }
    
    private void put(String message, String type)
    {
        Date now = new Date();
        
        try
        {
            JSONObject msg = new JSONObject();
            msg.put("ts", now.getTime());
            msg.put("date", df.format(now));
            msg.put("componentId", componentId);
            msg.put("instanceId", instanceId);
            msg.put("workerId", workerId);
            msg.put("type", type);
            msg.put("message", message);
            channel.basicPublish( "", queue_name, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.toString().getBytes());
        }
        catch (IOException | JSONException e)
        {
            log.error("Exception in put: " + e);
        }
    }
}
