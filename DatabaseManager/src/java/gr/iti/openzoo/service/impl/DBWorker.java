package gr.iti.openzoo.service.impl;

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
import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooInputConnection;
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
import gr.iti.openzoo.impl.OpenZooLoneWorker;
import java.net.UnknownHostException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class DBWorker extends OpenZooLoneWorker {

    private OpenZooInputConnection inConn = new OpenZooInputConnection(this, "db_input");
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
    
    private Mongo mongo;
    private DBCollection col_tweets, col_msgs;
    
    public DBWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- DBWorker()");
    }
    
    @Override
    public boolean doWork(Message message) {
        
        return true;
    }
    
    @Override
    public void run()
    {
        log.debug("-- DBWorker.run");
        
        if (!logConn.init())
        {
            log.error("Error by endpoint initialization");
            return;
        }
        
        logConn.debug("Running...");
        
        if (!inConn.init())
        {
            log.error("Error by endpoint initialization");
            logConn.error("Error by endpoint initialization");
            return;
        }
        
        // Access here the required parameters
        String mongo_database = getRequiredParameter("mongo_database");
        String mongo_collection_images = getRequiredParameter("mongo_collection_images");
        String mongo_collection_messages = getRequiredParameter("mongo_collection_messages");
        
        try
        {
            ServerAddress serverAdr = new ServerAddress(serviceParams.getMongo().getHost(), serviceParams.getMongo().getPort());
            MongoOptions options = new MongoOptions();
            options.connectionsPerHost = 10;
            
            mongo = new Mongo(serverAdr, options);
            mongo.setWriteConcern(WriteConcern.SAFE);
            DB db_se = mongo.getDB(mongo_database);
            db_se.authenticate(serviceParams.getMongo().getUser(), serviceParams.getMongo().getPasswd().toCharArray());
            col_tweets = db_se.getCollection(mongo_collection_images);
            col_msgs = db_se.getCollection(mongo_collection_messages);
        }
        catch (UnknownHostException ex) 
        {
            log.error("UnknownHostException during mongodb initialization: " + ex);
            logConn.error("UnknownHostException during mongodb initialization: " + ex);
            return;
        }
        
        
        log.info("Born!");
        Message message;
        
        while (!enough) 
        {
            message = inConn.getNext();
            
            if (message == null)
            {
                log.error("Received null message, aborting");
                logConn.error("Received null message, aborting");
                break;
            }
            else if (message.isEmpty())
            {
                log.error("Received empty message, discarding");
                logConn.error("Received empty message, discarding");
                inConn.ack(message);
                continue;
            }
            
            JSONObject pld = message.getPayload();
            JSONObject json;
            boolean success;
            try
            {
                success = true;
                
                if (pld.has("images"))
                {
                    for (int i = 0; i < pld.getJSONArray("images").length(); i++)
                    {
                        json = new JSONObject();
                        json.put("url", pld.getJSONArray("images").get(i));
                        json.put("date_posted", pld.getLong("date_posted"));
                        int ret = saveTweetUrl(json);

                        switch (ret)
                        {
                            case 0: log.debug("Inserted record"); logConn.debug("Inserted record"); break;
                            case 1: log.debug("Updated record"); logConn.debug("Updated record"); break;
                            default:log.error("Could not save tweet url"); logConn.error("Could not save tweet url"); 
                                    success = false;
                        }
                    }
                }
                                
                message.setSuccess(success);
                message.setProcessingEnd();
                saveMessage(message.getMessageJSON());
            }
            catch (JSONException e)
            {
                log.error("JSONException during saving tweet: " + e);
                logConn.error("JSONException during saving tweet: " + e);
            }
            
            inConn.ack(message);
        }
        
        mongo.close();

        log.info("Died!");
        logConn.info("Died!");
    }
    
    // -1: error, 0: created, 1: updated
    private int saveTweetUrl(JSONObject object)
    {
        String url = object.optString("url");
        
        if (url == null) return -1;
        
        BasicDBObject doc = new BasicDBObject("url", url);
        
        DBObject ret = col_tweets.findOne(doc);
        
        if (ret != null)
        {
            try
            {
                JSONObject json = new JSONObject(JSON.serialize(ret));
                json.put("t2", object.getLong("date_posted"));
                json.put("n", json.getInt("n") + 1);
                
                col_tweets.save((BasicDBObject) JSON.parse(json.toString()));
                
                return 1;
            }
            catch (JSONException e)
            {
                log.error("JSONException during updating record in MongoDB: " + e);
                logConn.error("JSONException during updating record in MongoDB: " + e);
                
                return -1;
            }
        }
        else
        {
            try
            {
                col_tweets.insert(doc.append("t1", object.getLong("date_posted")).append("t2", object.getLong("date_posted")).append("n", 1));
                
                return 0;
            }
            catch (JSONException e)
            {
                log.error("JSONException during creating record in MongoDB: " + e);
                logConn.error("JSONException during creating record in MongoDB: " + e);
                return -1;
            }
            catch (MongoException e)
            {
                log.error("MongoException during creating record in MongoDB: " + e);
                logConn.error("MongoException during creating record in MongoDB: " + e);
                return -1;
            }
        }
    }
    
    private boolean saveMessage(JSONObject msg)
    {
        try
        {
            BasicDBObject doc = (BasicDBObject) JSON.parse(msg.toString());
            
            col_msgs.insert(doc);
        }
        catch (MongoException e)
        {
            log.error("Message " + msg.optString("id") + " is already there");
            logConn.error("Message " + msg.optString("id") + " is already there");
            return false;
        }
        
        return true;
    }

    @Override
    public String publish(JSONObject obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
