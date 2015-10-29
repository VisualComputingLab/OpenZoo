package gr.iti.openzoo.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import gr.iti.openzoo.admin.KeyValueCommunication;
import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.admin.ParametersQueue;
import static gr.iti.openzoo.admin.ParametersQueue.Mapping.ALL;
import static gr.iti.openzoo.admin.ParametersQueue.Mapping.AVAIL;
import static gr.iti.openzoo.admin.ParametersQueue.Mapping.ROUTE;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class OpenZooOutputConnection {
       
    protected static Logger log = LogManager.getLogger(OpenZooOutputConnection.class.getName());
    private String id = null;
    
    private Channel channel;
    private ParametersQueue queueParams;
    private ParametersQueue.Mapping mapping;
    private String queue_name = null;
    private String exchange_name = null;
    private OpenZooWorker worker;
    private KeyValueCommunication kv;
    private String topologyId, componentId, instanceId, workerId;
    
    public OpenZooOutputConnection(OpenZooWorker ozw, String id)
    {
        worker = ozw;
        this.id = id;
    }
    
    public boolean init()
    {
        channel = worker.channel;
        kv = new KeyValueCommunication(worker.serviceParams.getRedis().getHost(), worker.serviceParams.getRedis().getPort());
                
        // get topology_id, component_id, name, worker_id
        topologyId = worker.serviceParams.getGeneral().getTopologyID();
        componentId = worker.serviceParams.getGeneral().getComponentID();
        instanceId = worker.serviceParams.getGeneral().getInstanceID();
        //String cname = worker.serviceParams.getGeneral().getName();
        workerId = new Throwable().getStackTrace()[1].getClassName().toString();
        
        // connection id has the form
        // component_id_source:worker_id_source:output_endpoint_id_source:component_id_target:worker_id_target:input_endpoint_id_target[:instance_id_target]
        // Since this is an output connection, we are the source (to the next component), so we don't care about the instance_id_target (if any)
        // We just need a queue or exchange name        
        
        try
        {
            String pattern = "connection:" + componentId + ":" + workerId + ":" + id + ":(.*):(.*):(.*)";
            String keyval = kv.getFirstHashKeyLike("topologies:" + topologyId, pattern);
            
            if (keyval == null)
            {
                log.error("Could not find an appropriate KV record: " + pattern);
                return false;
            }
                        
            JSONObject queue = new JSONObject(kv.getHashValue("topologies:" + topologyId, keyval));
            queueParams = new ParametersQueue(queue);
        }
        catch (JSONException e)
        {
            log.error("JSONException while creating JSONObject from KV: " + e);
        }
                
        mapping = queueParams.getMapping();
        
        // delete if exists
        kv.delHashValue("statistics:" + topologyId, "endpoint:messages:" + componentId + ":" + workerId + ":" + id + ":" + instanceId);
        kv.delHashValue("statistics:" + topologyId, "endpoint:bytes:" + componentId + ":" + workerId + ":" + id + ":" + instanceId);
        
        try
        {
            switch (mapping)
            {
                case AVAIL:
                    queue_name = queueParams.getQueueName();
                    channel.queueDeclare(queue_name, true, false, false, null);
                    break;
                case ALL:
                    exchange_name = queueParams.getExchangeName();
                    channel.exchangeDeclare(exchange_name, "topic", true);
                    break;
                case ROUTE:
                    exchange_name = queueParams.getExchangeName();
                    channel.exchangeDeclare(exchange_name, "topic", true);
                    break;
            }
        }
        catch (IOException e)
        {
            log.error("IOException in constructor: " + e);
            return false;
        }
        
        return true;
    }
    
    // put to ROUTE is put to ALL
    public void put(Message message)
    {        
        Message copy = new Message(message);
        copy.setProcessingEnd();
        copy.setOutputEndpointId(id);
        
        try
        {
            byte [] bytes = copy.getBytes();
            switch (mapping)
            {
                case AVAIL:
                    log.debug("Publishing to available");
                    channel.basicPublish( "", queue_name, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
                    break;
                case ALL:
                    log.debug("Publishing to all");
                    channel.basicPublish(exchange_name, "#", null, bytes);
                    break;
                case ROUTE:
                    log.debug("Publishing to selected");
                    channel.basicPublish(exchange_name, copy.getRouting_key(), null, bytes);
                    break;
            }
            
            kv.incrHashValue("statistics:" + topologyId, "endpoint:messages:" + componentId + ":" + workerId + ":" + id + ":" + instanceId, 1);
            kv.incrHashValue("statistics:" + topologyId, "endpoint:bytes:" + componentId + ":" + workerId + ":" + id + ":" + instanceId, bytes.length);
        }
        catch (IOException e)
        {
            log.error("IOException in put: " + e);
        }
    }
}
