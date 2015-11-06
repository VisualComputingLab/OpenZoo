package gr.iti.openzoo.service.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
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
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import gr.iti.openzoo.impl.OpenZooWorker;
import java.net.UnknownHostException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class MongoManagerWorker extends OpenZooWorker {
    
    private final OpenZooInputConnection inConn = new OpenZooInputConnection(this, "input");
    private final OpenZooOutputConnection outConnBroker = new OpenZooOutputConnection(this, "output_1");
    private final OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
    
    private Mongo mongo;
    private DBCollection col_tweets, col_msgs;
    
    public MongoManagerWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- MongoManagerWorker()");
    }
    
    @Override
    public boolean doWork(Message message) {
        
        return true;
    }
    
    @Override
    public String publish(JSONObject obj) {
        throw new UnsupportedOperationException("Not used.");
    }
    
    @Override
    public void run()
    {
        log.debug("-- MongoManagerWorker.run");
        
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
        
        if (!outConnBroker.init())
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
            
            JSONObject hdr = message.getHeader();
            JSONObject pld = message.getPayload();
            JSONObject json;
            boolean success;
            boolean fromSearch = hdr.optBoolean("search");
            if (fromSearch)
            {                
                try
                {
                    success = true;

                    String method = pld.optString("method", "newer");
                    int numResults = pld.optInt("num", 10);
                    if (numResults > 100) numResults = 100;
                    logConn.info("Received search request, method: " + method + ", num: " + numResults);
                    JSONArray results = getImages(method, numResults);
                    if (results == null)
                    {
                        success = false;
                    }
                    pld.put("results", results);
                    message.setPayload(pld);
                    message.setSuccess(success);
                    message.setProcessingEnd();
                    outConnBroker.put(message);
                }
                catch (JSONException e)
                {
                    log.error("JSONException during saving tweet: " + e);
                    logConn.error("JSONException during saving tweet: " + e);
                }
            }
            else
            {
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
                                case 0: log.debug("New image"); logConn.debug("New image"); break;
                                case 1: log.debug("Retweeted image"); logConn.debug("Retweeted image"); break;
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

    private JSONArray getImages(String method, int num)
    {
        JSONArray results = new JSONArray();
        
        DBCursor cursor;
        
        switch (method)
        {
            case "hotter": 
                cursor = col_tweets.find().sort(new BasicDBObject("n", -1)).limit(num);
                break;
            case "newer":
            default:
                cursor = col_tweets.find().sort(new BasicDBObject("t1", -1)).limit(num);
                break;
        }
        
        DBObject res;
        JSONObject tempJson;
        
        try
        {
            while (cursor.hasNext())
            {
                res = cursor.next();
                tempJson = new JSONObject(JSON.serialize(res));
                results.put(tempJson);
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException while retrieving images from mongo: " + e);
            return null;
        }
        
        return results;
    }
        
}
