package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooInputConnection;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
<#if QueueLogging??>
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
</#if>
import gr.iti.openzoo.impl.OpenZooWorker;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

<#if UsesMongo??>
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
</#if>

/**
 *
 * @author ${Author}
 */
public class ${WorkerID}Worker extends OpenZooWorker {

<#if HasInput??>
    private OpenZooInputConnection inConn = new OpenZooInputConnection(this, "input");
</#if>

<#if (NumOutputs > 0)>
 <#list 1..NumOutputs as i>
    private OpenZooOutputConnection outConn_${i} = new OpenZooOutputConnection(this, "output_${i}");
 </#list>
</#if>

<#if QueueLogging??>
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
</#if>

<#if UsesMongo??>
    private Mongo mongo;
    private DBCollection col_msgs;
</#if>
    
    public ${WorkerID}Worker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- ${WorkerID}Worker()");
    }
    
    @Override
    public boolean doWork(Message message) {
        
<#if IsBroker??>
        throw new UnsupportedOperationException("Not Used.");
<#else>
        // the actual message processing
        
        JSONObject pld = message.getPayload();
        
        try
        {
            pld.put("processed", true);
            message.setPayload(pld);
        }
        catch (JSONException e)
        {
            log.error("JSONException: " + e);
<#if QueueLogging??>
            logConn.error("JSONException: " + e);
</#if>
            return false;
        }
        
        return true;
</#if>
    }

    @Override
    public void run()
    {
        log.debug("-- ${WorkerID}Worker.run");
        
<#if QueueLogging??>
        if (!logConn.init())
        {
            log.error("Error by endpoint initialization");
            return;
        }
        
        logConn.debug("Running...");
</#if>

<#if HasInput??>
        if (!inConn.init())
        {
            log.error("Error by input endpoint initialization");
 <#if QueueLogging??>
            logConn.error("Error by input endpoint initialization");
 </#if>
            //return;
        }
</#if>

<#if (NumOutputs > 0)>
 <#list 1..NumOutputs as i> 
        if (!outConn_${i}.init())
        {
            log.error("Error by output_${i} endpoint initialization");
  <#if QueueLogging??>
            logConn.error("Error by output_${i} endpoint initialization");
  </#if>
            //return;
        }
 </#list>
</#if>

        
<#if RequiredParameters??>
        // Access here the required parameters
 <#list RequiredParameters as Parameter>
        String ${Parameter} = getRequiredParameter("${Parameter}");
 </#list>
</#if>

        // Do your initializing here
<#if UsesMongo??>
        try
        {
            // these could be provided over the required parameters
            String s_mongodb_db = "test_db";
            String s_mongodb_col = "messages";

            ServerAddress serverAdr = new ServerAddress(serviceParams.getMongo().getHost(), serviceParams.getMongo().getPort());
            MongoOptions options = new MongoOptions();
            options.connectionsPerHost = 10;
            
            mongo = new Mongo(serverAdr, options);
            mongo.setWriteConcern(WriteConcern.SAFE);
            DB db_se = mongo.getDB(s_mongodb_db);
            db_se.authenticate(serviceParams.getMongo().getUser(), serviceParams.getMongo().getPasswd().toCharArray());
            col_msgs = db_se.getCollection(s_mongodb_col);
        }
        catch (UnknownHostException ex) 
        {
            log.error("UnknownHostException during mongodb initialization: " + ex);
<#if QueueLogging??>
            logConn.error("UnknownHostException during mongodb initialization: " + ex);
</#if>
            return;
        }
</#if>
        
        log.info("Born!");
<#if QueueLogging??>
        logConn.info("Born!");
</#if>
        Message message;
        
        while (!enough) 
        {
<#if HasInput??>
            // get next message from queue
            message = inConn.getNext();
            
            if (message == null)
            {
                log.error("Received null message, aborting");
 <#if QueueLogging??>
                logConn.error("Received null message, aborting");
 </#if>
                break;
            }
            else if (message.isEmpty())
            {
                log.error("Received empty message, discarding");
 <#if QueueLogging??>
                logConn.error("Received empty message, discarding");
 </#if>
                inConn.ack(message);
                continue;
            }

<#else>
            // create new message
            message = createEmptyMessage();

</#if>
            
<#if IsBroker??>
            // acknowledge message and put results into request map
            inConn.ack(message);
            requests.put(message.getID(), message.getPayload());
<#else>
            // Do your processing here
            boolean success = doWork(message);
            
            if (success)
            {
                message.setSuccess(success);
                
 <#if (NumOutputs > 0)>
                // send results to next component through the first output
                outConn_1.put(message);
 <#else>
                // do something with the final message, e.g. write it to the mongo
                message.setProcessingEnd();
  <#if UsesMongo??>
                saveMessage(message.getMessageJSON());
  </#if>
 </#if>
            }

 <#if HasInput??>
            // acknowledge incomming message
            inConn.ack(message);
 </#if>
</#if>
        }
        
        // Do your cleaning here
<#if IsBroker??>
        requests.clear();
</#if>
        log.info("Died!");
<#if QueueLogging??>
        logConn.info("Died!");
</#if>
    }

<#if UsesMongo??>
    private boolean saveMessage(JSONObject msg)
    {
        try
        {
            BasicDBObject doc = (BasicDBObject) JSON.parse(msg.toString());
            
            //col_msgs.save(doc);
            col_msgs.insert(doc);
        }
        catch (MongoException e)
        {
            log.error("Message " + msg.optString("id") + " is already there");
<#if QueueLogging??>
            logConn.error("Message " + msg.optString("id") + " is already there");
</#if>
            return false;
        }
        
        return true;
    }
</#if>

    @Override
    public String publish(JSONObject obj) {
 
<#if IsBroker??>
        Message message = createEmptyMessage();
        
        try
        {            
            JSONObject pld = message.getPayload();

            // put the whole input json or a part of it in the payload
            // send it to some output endpoint
            // and put it to the request map
            pld.put("content", obj);

            message.setPayload(pld);            
            message.setSuccess(true);
            
            outConn_1.put(message);
            
            return putRequest(message);
        }
        catch (JSONException ex) 
        {
            log.error("JSONException during publishing message: " + ex);
        }
        
        return null;
<#else>
        throw new UnsupportedOperationException("Not Used.");
</#if>
    }

}
