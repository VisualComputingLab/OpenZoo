package gr.iti.openzoo.ui;

import com.mongodb.BasicDBList;
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
import gr.iti.openzoo.pojos.Server;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.pojos.TopologyGraphConnection;
import gr.iti.openzoo.pojos.TopologyGraphNode;
import gr.iti.openzoo.pojos.WarFile;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    
    public void putServer(Server srv)
    {
        if (srv == null) return;
        
        BasicDBObject doc = new BasicDBObject("_id", srv.getName());
        //col_servers.remove(doc);
        doc.append("address", srv.getAddress());
        doc.append("port", srv.getPort());
        doc.append("user", srv.getUser());
        doc.append("passwd", srv.getPasswd());
        doc.append("status", srv.getStatus());
        
        try
        {
            col_servers.save(doc);
        }
        catch (MongoException e)
        {
            error("MongoException during creating server in MongoDB", e);
        }
    }
    
    public ArrayList<Server> getServers()
    {
        return getServers(false);
    }
    
    public ArrayList<Server> getServers(boolean delete)
    {
        ArrayList<Server> allServers = new ArrayList<>();
        DBCursor cursor = col_servers.find();
        
        DBObject res;
        JSONObject tempJson;
        
        try
        {
            while (cursor.hasNext())
            {
                res = cursor.next();
                tempJson = new JSONObject(JSON.serialize(res));
                Server srv = new Server(tempJson.getString("_id"), tempJson.getString("address"), tempJson.getInt("port"), tempJson.getString("user"), tempJson.getString("passwd"));
                allServers.add(srv);
            }
            
            if (delete) col_servers.remove(new BasicDBObject());
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving servers from mongo", e);
            return null;
        }
        
        return allServers;
    }
    
    public Server getServer(String servername)
    {
        return getServer(servername, false);
    }
    
    public Server getServer(String servername, boolean delete)
    {
        BasicDBObject doc = new BasicDBObject("_id", servername);
        DBObject res = col_servers.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        Server srv;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            srv = new Server(tempJson.getString("_id"), tempJson.getString("address"), tempJson.getInt("port"), tempJson.getString("user"), tempJson.getString("passwd"));
            
            if (delete) col_servers.remove(doc);
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving server from mongo", e);
            return null;
        }
        
        return srv;
    }
    
    public void putWarFile(WarFile war)
    {
        if (war == null) return;
        
        BasicDBObject doc = new BasicDBObject("_id", war.getComponent_id());
        //col_warfiles.remove(doc);
        doc.append("version", war.getVersion());
        doc.append("filename", war.getFilename());
        doc.append("service_path", war.getService_path());
        doc.append("name", war.getName());
        doc.append("description", war.getDescription());
        doc.append("type", war.getType());
        doc.append("status", war.getStatus());
        
        ArrayList<String> reqs = war.getRequires();
        if (reqs != null && reqs.size() > 0)
        {
            //JSONArray reqjar = new JSONArray();
            doc.append("requires", reqs);
        }


        ArrayList<Worker> works= war.getWorkers();
        if (works != null && works.size() > 0)
        {
//            ArrayList<JSONObject> work_jsons = new ArrayList<>();
//            for (Worker w : works)
//                work_jsons.add(w.toJSON());
            ArrayList<BasicDBObject> work_bsons = new ArrayList<>();
            for (Worker w : works)
                work_bsons.add((BasicDBObject) JSON.parse(w.toJSON().toString()));

            doc.append("workers", work_bsons);
        }
        
        try
        {
            col_warfiles.save(doc);
        }
        catch (MongoException e)
        {
            error("MongoException during creating warfile in MongoDB", e);
        }
    }
        
    public ArrayList<WarFile> getWarFiles()
    {
        return getWarFiles(false);
    }
    
    public ArrayList<WarFile> getWarFiles(boolean delete)
    {
        ArrayList<WarFile> allWarfiles = new ArrayList<>();
        DBCursor cursor = col_warfiles.find();
        
        DBObject res;
        JSONObject tempJson;
        
        try
        {
            while (cursor.hasNext())
            {
                res = cursor.next();
                tempJson = new JSONObject(JSON.serialize(res));
                JSONObject warconfig = new JSONObject();
                
                JSONObject warservice = new JSONObject();
                warservice.put("component_id", tempJson.getString("_id"));
                warservice.put("name", tempJson.getString("name"));
                warservice.put("path", tempJson.getString("service_path"));
                warservice.put("description", tempJson.getString("description"));
                warservice.put("type", tempJson.getString("type"));                
                warconfig.put("service", warservice);
                
                if (tempJson.has("requires"))
                {
                    warconfig.put("requires", tempJson.getJSONArray("requires"));
                }
                
                if (tempJson.has("workers"))
                {
                    warconfig.put("workers", tempJson.getJSONArray("workers"));
                }
                
                WarFile war = new WarFile(tempJson.getString("filename"), tempJson.getString("version"), tempJson.getString("status"), warconfig);
                allWarfiles.add(war);
            }
            
            if (delete) col_warfiles.remove(new BasicDBObject());
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving warfiles from mongo", e);
            return null;
        }
        
        return allWarfiles;
    }
    
    public WarFile getWarFile(String warcompid)
    {
        return getWarFile(warcompid, false);
    }
    
    public WarFile getWarFile(String warcompid, boolean delete)
    {
        BasicDBObject doc = new BasicDBObject("_id", warcompid);
        DBObject res = col_warfiles.findOne(doc);
        if (res == null) return null;
        
        JSONObject tempJson;
        WarFile war;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            JSONObject warconfig = new JSONObject();
            
            JSONObject warservice = new JSONObject();
            warservice.put("component_id", tempJson.getString("_id"));
            warservice.put("name", tempJson.getString("name"));
            warservice.put("path", tempJson.getString("service_path"));
            warservice.put("description", tempJson.getString("description"));
            warservice.put("type", tempJson.getString("type"));                
            warconfig.put("service", warservice);

            if (tempJson.has("requires"))
            {
                warconfig.put("requires", tempJson.getJSONArray("requires"));
            }

            if (tempJson.has("workers"))
            {
                warconfig.put("workers", tempJson.getJSONArray("workers"));
            }

            war = new WarFile(tempJson.getString("filename"), tempJson.getString("version"), tempJson.getString("status"), warconfig);
             
            if (delete) col_warfiles.remove(doc);
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving war file from mongo", e);
            return null;
        }
        
        return war;
    }
    
    public void putTopology(Topology topo)
    {
        if (topo == null) return;
        
        BasicDBObject doc = new BasicDBObject("_id", topo.getName());
        doc.append("description", topo.getDescription());
        BasicDBObject rabbit = new BasicDBObject("host", topo.getRabbit_host()).append("port", topo.getRabbit_port()).append("user", topo.getRabbit_user()).append("passwd", topo.getRabbit_passwd());
        doc.append("rabbit", rabbit);
        BasicDBObject mongodb = new BasicDBObject("host", topo.getMongo_host()).append("port", topo.getMongo_port()).append("user", topo.getMongo_user()).append("passwd", topo.getMongo_passwd());
        doc.append("mongo", mongodb);
        
        ArrayList<TopologyGraphNode> nodes = topo.getNodes();
        ArrayList<BasicDBObject> dbo_nodes = new ArrayList<>();
        for (TopologyGraphNode nod : nodes)
        {
            dbo_nodes.add(new BasicDBObject("name", nod.getName()).append("instances", nod.getInstances()).append("threadspercore", nod.getThreadspercore()).append("requires", nod.getRequirements()));
        }       
        doc.append("nodes", dbo_nodes);
        
        ArrayList<TopologyGraphConnection> connections = topo.getConnections();
        ArrayList<BasicDBObject> dbo_connections = new ArrayList<>();
        for (TopologyGraphConnection conn : connections)
        {
            BasicDBObject dbo_conn = new BasicDBObject("s_component", conn.getSource_component());
            dbo_conn.append("s_worker", conn.getSource_worker());
            dbo_conn.append("s_endpoint", conn.getSource_endpoint());
            dbo_conn.append("t_component", conn.getTarget_component());
            dbo_conn.append("t_worker", conn.getTarget_worker());
            dbo_conn.append("t_endpoint", conn.getTarget_endpoint());
            dbo_conn.append("t_instance", conn.getTarget_instance());
            dbo_conn.append("mapping", conn.getMapping());
            if (conn.getQueue_name() != null && !conn.getQueue_name().isEmpty())
                dbo_conn.append("queue_name", conn.getQueue_name());
            if (conn.getExchange_name() != null && !conn.getExchange_name().isEmpty())
                dbo_conn.append("exchange_name", conn.getExchange_name());
            if (conn.getRouting_keys() != null && !conn.getRouting_keys().isEmpty())
                dbo_conn.append("routing_keys", conn.getRouting_keys());
            dbo_connections.add(dbo_conn);
        }
        doc.append("connections", dbo_connections);
        
        if (topo.hasGraph_object())
            doc.append("graph_object", topo.getGraph_object().toString());
        
        if (topo.hasConf_object())
            doc.append("conf_object", topo.getConf_object().toString());
        
        try
        {
            col_topologies.save(doc);
        }
        catch (MongoException e)
        {
            error("MongoException during creating topology in MongoDB", e);
        }
    }
    
    public ArrayList<Topology> getTopologies()
    {
        return getTopologies(false);
    }
    
    public ArrayList<Topology> getTopologies(boolean delete)
    {
        ArrayList<Topology> allTopologies = new ArrayList<>();
        DBCursor cursor = col_topologies.find();
        
        DBObject res;
        JSONObject tempJson;
        
        try
        {
            while (cursor.hasNext())
            {
                res = cursor.next();
                tempJson = new JSONObject(JSON.serialize(res));
                JSONObject rabbit = tempJson.getJSONObject("rabbit");
                JSONObject mongodb = tempJson.getJSONObject("mongo");
                Topology topo = new Topology(tempJson.getString("_id"), tempJson.getString("description"), rabbit.getString("host"), rabbit.getInt("port"), rabbit.getString("user"), rabbit.getString("passwd"), mongodb.getString("host"), mongodb.getInt("port"), mongodb.getString("user"), mongodb.getString("passwd"));
                
                ArrayList<BasicDBObject> dbo_nodes = (ArrayList<BasicDBObject>) res.get("nodes");
                ArrayList<TopologyGraphNode> nodes = new ArrayList<>();
                
                for (BasicDBObject bdbo : dbo_nodes)
                {
                    TopologyGraphNode tgn = new TopologyGraphNode(bdbo.getString("name"), bdbo.getInt("instances"), bdbo.getInt("threadspercore"), (HashMap<String, String>) bdbo.get("requires"));
                    nodes.add(tgn);
                }
                topo.setNodes(nodes);
                
                ArrayList<BasicDBObject> dbo_connections = (ArrayList<BasicDBObject>) res.get("connections");
                ArrayList<TopologyGraphConnection> connections = new ArrayList<>();
                
                for (BasicDBObject bdbo : dbo_connections)
                {
                    BasicDBList bdbl = (BasicDBList) bdbo.get("routing_keys");
                    HashSet<String> hset = null;
                    if (bdbl != null)
                        hset = new HashSet<>(bdbl.keySet());
                    TopologyGraphConnection tgc = new TopologyGraphConnection(bdbo.getString("s_component"), bdbo.getString("s_worker"), bdbo.getString("s_endpoint"),
                                                                                bdbo.getString("t_component"), bdbo.getString("t_worker"), bdbo.getString("t_endpoint"), bdbo.getInt("t_instance"),
                                                                                bdbo.getInt("mapping"), bdbo.getString("queue_name", null), bdbo.getString("exchange_name", null), hset);
                    connections.add(tgc);
                }
                topo.setConnections(connections);
                
                if (res.containsField("graph_object"))
                    topo.setGraph_object(new JSONObject(tempJson.getString("graph_object")));
                
                if (res.containsField("conf_object"))
                    topo.setConf_object(new JSONObject(tempJson.getString("conf_object")));       
                
                allTopologies.add(topo);                
            }
            
            if (delete) col_topologies.remove(new BasicDBObject());
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving topologies from mongo", e);
            return null;
        }
        
        return allTopologies;
    }
    
    public Topology getTopology(String toponame)
    {
        return getTopology(toponame, false);
    }
    
    public Topology getTopology(String toponame, boolean delete)
    {
        BasicDBObject doc = new BasicDBObject("_id", toponame);
        DBObject res = col_topologies.findOne(doc);
        
        if (res == null) return null;
        
        JSONObject tempJson;
        Topology topo;
        
        try
        {
            tempJson = new JSONObject(JSON.serialize(res));
            
            JSONObject rabbit = tempJson.getJSONObject("rabbit");
            JSONObject mongodb = tempJson.getJSONObject("mongo");
            topo = new Topology(tempJson.getString("_id"), tempJson.getString("description"), rabbit.getString("host"), rabbit.getInt("port"), rabbit.getString("user"), rabbit.getString("passwd"), mongodb.getString("host"), mongodb.getInt("port"), mongodb.getString("user"), mongodb.getString("passwd"));
                
            ArrayList<BasicDBObject> dbo_nodes = (ArrayList<BasicDBObject>) res.get("nodes");
            ArrayList<TopologyGraphNode> nodes = new ArrayList<>();
                
            for (BasicDBObject bdbo : dbo_nodes)
            {
                TopologyGraphNode tgn = new TopologyGraphNode(bdbo.getString("name"), bdbo.getInt("instances"), bdbo.getInt("threadspercore"), (HashMap<String, String>) bdbo.get("requires"));
                nodes.add(tgn);
            }
            topo.setNodes(nodes);

            ArrayList<BasicDBObject> dbo_connections = (ArrayList<BasicDBObject>) res.get("connections");
            ArrayList<TopologyGraphConnection> connections = new ArrayList<>();

            for (BasicDBObject bdbo : dbo_connections)
            {
                BasicDBList bdbl = (BasicDBList) bdbo.get("routing_keys");
                HashSet<String> hset = null;
                if (bdbl != null)
                    hset = new HashSet<>(bdbl.keySet());
                TopologyGraphConnection tgc = new TopologyGraphConnection(bdbo.getString("s_component"), bdbo.getString("s_worker"), bdbo.getString("s_endpoint"),
                                                                            bdbo.getString("t_component"), bdbo.getString("t_worker"), bdbo.getString("t_endpoint"), bdbo.getInt("t_instance"),
                                                                            bdbo.getInt("mapping"), bdbo.getString("queue_name", null), bdbo.getString("exchange_name", null), hset);
                connections.add(tgc);
            }
            topo.setConnections(connections);

            if (res.containsField("graph_object"))
                topo.setGraph_object(new JSONObject(tempJson.getString("graph_object")));

            if (res.containsField("conf_object"))
                topo.setConf_object(new JSONObject(tempJson.getString("conf_object")));

             
            if (delete) col_topologies.remove(doc);
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving topology from mongo", e);
            return null;
        }
        
        return topo;
    }
            
    public MessageStatistics getEndpointStats(String toponame)
    {   
        // .find({topo: toponame})
        BasicDBObject doc = new BasicDBObject("topo", toponame);
        DBCursor cursor = col_statistics.find(doc);
        
        DBObject res;
        JSONObject tempJson;
        MessageStatistics ms = new MessageStatistics();
        
        try
        {
            while (cursor.hasNext())
            {
                res = cursor.next();
                tempJson = new JSONObject(JSON.serialize(res));
                
                String key = tempJson.getString("component") + ":" + tempJson.getString("worker") + ":" + tempJson.getString("endpoint") + ":" + tempJson.getInt("instance");
                long messages = tempJson.getLong("messages");
                long bytes = tempJson.getLong("bytes");
                
                ms.addEndpointMessages(key, messages);
                ms.addEndpointBytes(key, bytes);
            }
        }
        catch (JSONException e)
        {
            error("JSONException while retrieving statistics from mongo", e);
            return null;
        }
        
        return ms;
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
