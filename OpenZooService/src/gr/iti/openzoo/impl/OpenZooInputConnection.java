package gr.iti.openzoo.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import gr.iti.openzoo.admin.KeyValueCommunication;
import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.admin.ParametersQueue;
import gr.iti.openzoo.admin.ParametersQueue.Mapping;
import java.io.IOException;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class OpenZooInputConnection {

    protected static Logger log = LogManager.getLogger(OpenZooInputConnection.class.getName());
    private String id = null;
    
    private QueueingConsumer qconsumer;
    private Channel channel;
    private ParametersQueue queueParams;
    private Mapping mapping;
    private OpenZooWorker worker;
    private KeyValueCommunication kv;
    private String topologyId, componentId, instanceId, workerId;
    
    public OpenZooInputConnection(OpenZooWorker ozw, String id)
    {
        worker = ozw;
        this.id = id;
    }

    public boolean init()
    {
        channel = worker.channel;
        kv = new KeyValueCommunication(worker.serviceParams.getRedis().getHost(), worker.serviceParams.getRedis().getPort());
                       
        // get topologyId, component_id, instance_id, worker_id
        topologyId = worker.serviceParams.getGeneral().getTopologyID();
        componentId = worker.serviceParams.getGeneral().getComponentID();
        instanceId = worker.serviceParams.getGeneral().getInstanceID();
        //String cname = worker.serviceParams.getGeneral().getName();
        workerId = new Throwable().getStackTrace()[1].getClassName().toString();
        
        // connection id has the form
        // component_id_source:worker_id_source:output_endpoint_id_source:component_id_target:worker_id_target:input_endpoint_id_target[:instance_id_target]
        // Since this is an input connection, we are the target (of the previous component)
        // If the connection is of type ROUTE, we use the instance_id_target to obtain the correct parameters (routing keys) for this instance
        // If the connection is of type ANY or ALL, we don't care, we just need a queue or exchange name
        // So we first check if key with instance_id_target exists, and if not then we check without the instance_id_target
        
        try
        {
            String pattern = "connection:(.*):(.*):(.*):" + componentId + ":" + workerId + ":" + id + ":" + instanceId;
            String keyval = kv.getFirstHashKeyLike("topologies:" + topologyId, pattern);
            if (keyval == null)
            {
                pattern = "connection:(.*):(.*):(.*):" + componentId + ":" + workerId + ":" + id;
                keyval = kv.getFirstHashKeyLike("topologies:" + topologyId, pattern);
            }
            
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
            return false;
        }
        
        // delete if exists
        kv.delHashValue("statistics:" + topologyId, "endpoint:messages:" + componentId + ":" + workerId + ":" + id + ":" + instanceId);
        kv.delHashValue("statistics:" + topologyId, "endpoint:bytes:" + componentId + ":" + workerId + ":" + id + ":" + instanceId);
        
        qconsumer = new QueueingConsumer(channel);
        String queue_name, exchange_name;
        mapping = queueParams.getMapping();

        try
        {
            switch (mapping)
            {
                case AVAIL:
                    queue_name = queueParams.getQueueName();
                    channel.queueDeclare(queue_name, true, false, false, null);
                    channel.basicConsume(queue_name, false, qconsumer);
                    break;
                case ALL:
                    exchange_name = queueParams.getExchangeName();
                    queue_name = exchange_name + "_QUEUE_" + instanceId;
                    channel.queueDeclare(queue_name, true, false, false, null);
                    channel.exchangeDeclare(exchange_name, "topic", true);
                    channel.queueBind(queue_name, exchange_name, "#");
                    channel.basicConsume(queue_name, false, qconsumer);
                    break;
                case ROUTE:
                    exchange_name = queueParams.getExchangeName();
                    queue_name = exchange_name + "_QUEUE_" + instanceId;
                    channel.queueDeclare(queue_name, true, false, false, null);
                    channel.exchangeDeclare(exchange_name, "topic", true);
                    HashSet<String> rkeys = queueParams.getRoutingKeys();
                    for (String rk : rkeys)
                    {
                        channel.queueBind(queue_name, exchange_name, rk);
                    }
                    channel.basicConsume(queue_name, false, qconsumer);
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
    
    // blocking
    public Message getNext()
    {
        Message message = null;

        try
        {
            message = new Message(qconsumer.nextDelivery(), componentId, instanceId, workerId, id);
            log.debug("Consumed message");
        }
        catch (InterruptedException ex) 
        {
            log.error("InterruptedException during message delivery: " + ex);
        }
        catch (ConsumerCancelledException ex) 
        {
            log.error("ConsumerCancelledException during message delivery: " + ex);
        }
        catch (ShutdownSignalException ex) 
        {
        }

        return message;
    }
    
    public boolean ack(Message message)
    {
        log.debug("Acknowledging message");
        boolean res = true;
        try
        {
            channel.basicAck(message.getDeliveryTag(), false);
        }
        catch (IOException e)
        {
            log.error("Could not deliver acknowledgment, aborting (consumer exit?): " + e);
            res = false;
        }
        
        kv.incrHashValue("statistics:" + topologyId, "endpoint:messages:" + componentId + ":" + workerId + ":" + id + ":" + instanceId, 1);
        kv.incrHashValue("statistics:" + topologyId, "endpoint:bytes:" + componentId + ":" + workerId + ":" + id + ":" + instanceId, message.getBytes().length);
        
        return res;
    }
}
