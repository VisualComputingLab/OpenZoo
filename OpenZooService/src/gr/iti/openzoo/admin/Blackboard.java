package gr.iti.openzoo.admin;

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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 * 
 * The blackboard is a database in mongodb with the following collections:
 * Servers: Each server is a document in this collection
 * Warfiles: Each war file is a document in this collection
 * Topologies: Each topology is a document in this collection
 * Statistics: Each document in this collection is holding the statistics of an instance of a service in a specific topology
 */
public class Blackboard {
            
    private Mongo mongo;
    private DBCollection col_warfiles, col_servers, col_topologies, col_statistics;
    private String user, passwd, database;
    
//    private static JedisPool pool;
       
    public Blackboard(String m_host, int m_port, String m_user, String m_passwd, String m_db)
    {        
        info("Initializing blackboard on " + m_host + ":" + m_port);
        
        try
        {
            ServerAddress serverAdr = new ServerAddress(m_host, m_port);
            MongoOptions options = new MongoOptions();
            options.connectionsPerHost = 10;
            
            user = m_user;
            passwd = m_passwd;
            database = m_db;
            
            mongo = new Mongo(serverAdr, options);
            mongo.setWriteConcern(WriteConcern.SAFE);
            DB db_se = mongo.getDB(m_db);
            db_se.authenticate(m_user, m_passwd.toCharArray());
            col_warfiles = db_se.getCollection("warfiles");
            col_servers = db_se.getCollection("servers");
            col_topologies = db_se.getCollection("topologies");
            col_statistics = db_se.getCollection("statistics");
        }
        catch (UnknownHostException ex) 
        {
            error("UnknownHostException during blackboard initialization", ex);
        }
    }
    
    public String getHost()
    {
        return mongo.getAddress().getHost();
    }
    
    public int getPort()
    {
        return mongo.getAddress().getPort();
    }
    
    public String getUser()
    {
        return user;
    }
    
    public String getPasswd()
    {
        return passwd;
    }
    
    public String getDatabase()
    {
        return database;
    }
    
    public void stop()
    {
        mongo.close();
        info("Blackboard closed!");
    }
                    
    public void incEndpointStats(String toponame, String compo, String worker, String endpoint, int instance, long msgs, long bts)
    {
        BasicDBObject doc = new BasicDBObject("topo", toponame).append("component", compo).append("worker", worker).append("endpoint", endpoint).append("instance", instance);
        BasicDBObject res = (BasicDBObject) col_statistics.findOne(doc);
        
        if (res == null)
        {
            BasicDBObject docNew = new BasicDBObject("topo", toponame).append("component", compo).append("worker", worker).append("endpoint", endpoint).append("instance", instance);
            docNew.append("messages", 0);
            docNew.append("bytes", 0);
            col_statistics.insert(docNew);
            res = (BasicDBObject) col_statistics.findOne(doc);
        }
                
        try
        {
            if (msgs == -1)
                res.put("messages", 0);
            else
                res.put("messages", res.getLong("messages") + msgs);
            
            if (bts == -1)
                res.put("bytes", 0);
            else
                res.put("bytes", res.getLong("bytes") + bts);
            
            col_statistics.save(res);
        }
        catch (MongoException e)
        {
            error("MongoException during updating statistics in MongoDB", e);
        }
    }
        
    public String getRequiredParameter(String toponame, String compo, String param)
    {
//        BasicDBObject doc = new BasicDBObject("_id", toponame);
//        DBObject res = col_topologies.findOne(doc);
//        if (res == null) return null;
        
        JSONObject aNode;
        
        try
        {
            aNode = getNode(toponame, compo);
            
            if (aNode != null)
            {
                JSONObject reqs = aNode.getJSONObject("requires");
                return reqs.optString(param);
            }
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving required parameter from mongo", e);
            return null;
        }
        
        return null;
    }
    
    public JSONObject getNode(String toponame, String compo)
    {
        BasicDBObject doc = new BasicDBObject("_id", toponame);
        DBObject res = col_topologies.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        JSONObject aNode;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            JSONArray nodesarray = tempJson.optJSONArray("nodes");
            
            if (nodesarray == null || nodesarray.length() == 0)
                return null;
            
            for (int i = 0; i < nodesarray.length(); i++)
            {
                aNode = nodesarray.getJSONObject(i);
                if (aNode.getString("name").equals(compo))
                {
                    return aNode;
                }
            }
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving required parameter from mongo", e);
            return null;
        }
        
        return null;
    }
    
    public JSONObject getRabbit(String toponame)
    {
        BasicDBObject doc = new BasicDBObject("_id", toponame);
        DBObject res = col_topologies.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            return tempJson.optJSONObject("rabbit");
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving rabbit parameters from mongo", e);
            return null;
        }
    }
    
    public JSONObject getMongo(String toponame)
    {
        BasicDBObject doc = new BasicDBObject("_id", toponame);
        DBObject res = col_topologies.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            return tempJson.optJSONObject("mongo");
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving mongo parameters from mongo", e);
            return null;
        }
    }
    
    public JSONObject getQueueParametersIn(String topologyId, String componentId, String workerId, String id, int instanceId)
    {
        BasicDBObject doc = new BasicDBObject("_id", topologyId);
        DBObject res = col_topologies.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            JSONArray connections = tempJson.getJSONArray("connections");
            JSONObject aConn;
            for (int i = 0; i < connections.length(); i++)
            {
                aConn = connections.getJSONObject(i);
                if (    aConn.getString("t_component").equalsIgnoreCase(componentId) &&
                        aConn.getString("t_worker").equalsIgnoreCase(workerId) &&
                        aConn.getString("t_endpoint").equalsIgnoreCase(id) &&
                        aConn.getInt("t_instance") == instanceId)
                {
                    JSONObject qp = new JSONObject();
                    
                    int i_map = aConn.getInt("mapping");
        
                    switch (i_map)
                    {
                        case 2: qp.put("mapping", 2);
                                qp.put("exchange_name", aConn.getString("exchange_name"));
                                qp.put("routing_keys", aConn.getJSONArray("routing_keys"));
                                break;
                            
                        case 1: qp.put("mapping", 1);
                                qp.put("exchange_name", aConn.getString("exchange_name"));
                                break;
                            
                        default:qp.put("mapping", 0);
                                qp.put("queue_name", aConn.getString("queue_name"));
                                break;
                    }  
                    
                    return qp;
                }
            }
            
            return null;
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving rabbit parameters from mongo", e);
            return null;
        }
    }
    
    public JSONObject getQueueParametersIn(String topologyId, String componentId, String workerId, String id)
    {
        BasicDBObject doc = new BasicDBObject("_id", topologyId);
        DBObject res = col_topologies.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            JSONArray connections = tempJson.getJSONArray("connections");
            JSONObject aConn;
            for (int i = 0; i < connections.length(); i++)
            {
                aConn = connections.getJSONObject(i);
                if (    aConn.getString("t_component").equalsIgnoreCase(componentId) &&
                        aConn.getString("t_worker").equalsIgnoreCase(workerId) &&
                        aConn.getString("t_endpoint").equalsIgnoreCase(id))
                {
                    JSONObject qp = new JSONObject();
                    
                    int i_map = aConn.getInt("mapping");
        
                    switch (i_map)
                    {
                        case 2: qp.put("mapping", 2);
                                qp.put("exchange_name", aConn.getString("exchange_name"));
                                qp.put("routing_keys", aConn.getJSONArray("routing_keys"));
                                break;
                            
                        case 1: qp.put("mapping", 1);
                                qp.put("exchange_name", aConn.getString("exchange_name"));
                                break;
                            
                        default:qp.put("mapping", 0);
                                qp.put("queue_name", aConn.getString("queue_name"));
                                break;
                    }  
                    
                    return qp;
                }
            }
            
            return null;
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving rabbit parameters from mongo", e);
            return null;
        }
    }
    
    public JSONObject getQueueParametersOut(String topologyId, String componentId, String workerId, String id)
    {
        BasicDBObject doc = new BasicDBObject("_id", topologyId);
        DBObject res = col_topologies.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            JSONArray connections = tempJson.getJSONArray("connections");
            JSONObject aConn;
            for (int i = 0; i < connections.length(); i++)
            {
                aConn = connections.getJSONObject(i);
                if (    aConn.getString("s_component").equalsIgnoreCase(componentId) &&
                        aConn.getString("s_worker").equalsIgnoreCase(workerId) &&
                        aConn.getString("s_endpoint").equalsIgnoreCase(id))
                {
                    JSONObject qp = new JSONObject();
                    
                    int i_map = aConn.getInt("mapping");
        
                    switch (i_map)
                    {
                        case 2: qp.put("mapping", 2);
                                qp.put("exchange_name", aConn.getString("exchange_name"));
                                qp.put("routing_keys", aConn.getJSONArray("routing_keys"));
                                break;
                            
                        case 1: qp.put("mapping", 1);
                                qp.put("exchange_name", aConn.getString("exchange_name"));
                                break;
                            
                        default:qp.put("mapping", 0);
                                qp.put("queue_name", aConn.getString("queue_name"));
                                break;
                    }  
                    
                    return qp;
                }
            }
            
            return null;
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving rabbit parameters from mongo", e);
            return null;
        }
    }
    
    private void info(String s)
    {
        System.out.println(s);
    }
    
    private void error(String s, Exception ex)
    {
        System.err.println(s);
        if (ex != null)
            ex.printStackTrace();
    }
}
