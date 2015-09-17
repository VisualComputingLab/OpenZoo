package gr.iti.openzoo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Topology {

    private String name;
    private String description;
    
    private String rabbit_host;
    private int rabbit_port;
    private String rabbit_user;
    private String rabbit_passwd;
    private String mongo_host;
    private int mongo_port;
    private String mongo_user;
    private String mongo_passwd;
    
    private ArrayList<TopologyGraphNode> nodes;
    private ArrayList<TopologyGraphConnection> connections;
    
    private JSONObject graph_object = null;
    private JSONObject server_config = null;
    
    //private static Utilities util = new Utilities();

    public Topology(String n, String d, String rh, int rp, String ru, String rps, String mh, int mp, String mu, String mps)
    {
        name = n;
        description = d;
        rabbit_host = rh;
        rabbit_port = rp;
        rabbit_user = ru;
        rabbit_passwd = rps;
        mongo_host = mh;
        mongo_port = mp;
        mongo_user = mu;
        mongo_passwd = mps;
        
        nodes = new ArrayList<>();
        connections = new ArrayList<>();
    }
    
    public Topology(Map<String, String> prop)
    {
        String value;
        String [] split;
        
        nodes = new ArrayList<>();
        connections = new ArrayList<>();
        
        HashMap<String, String> reqs = new HashMap<>();
        
        try
        {
            for (String key : prop.keySet())
            {
                switch (key)
                {
                    case "name": name = prop.get("name"); break;
                    case "description": description = prop.get(key); break;
                    case "rabbit:host": rabbit_host = prop.get(key); break;
                    case "rabbit:port": rabbit_port = Integer.parseInt(prop.get(key)); break;
                    case "rabbit:user": rabbit_user = prop.get(key); break;
                    case "rabbit:passwd": rabbit_passwd = prop.get(key); break;
                    case "mongo:host": mongo_host = prop.get(key); break;
                    case "mongo:port": mongo_port = Integer.parseInt(prop.get(key)); break;
                    case "mongo:user": mongo_user = prop.get(key); break;
                    case "mongo:passwd": mongo_passwd = prop.get(key); break;
                    case "graph_object": graph_object = new JSONObject(prop.get(key)); break;
                    case "server_config": server_config = new JSONObject(prop.get(key)); break;
                    default:
                        if (key.startsWith("node:"))
                        {
                            split = key.split(":", 2);
                            value = prop.get(key);
                            JSONObject json = new JSONObject(value);
                            TopologyGraphNode nod = new TopologyGraphNode(split[1], json);
                            nodes.add(nod);
                        }
                        else if (key.startsWith("requires:"))
                        {
                            // store requirements until all nodes are parsed
                            // afterwards, we can add the requirements to the right node
                            split = key.split(":", 2);
                            value = prop.get(key);
                            reqs.put(split[1], value);
                        }
                        else if (key.startsWith("connection:"))
                        {
                            split = key.split(":", 8);
                            int target_instance = -1;
                            if (split.length > 7)
                                target_instance = Integer.parseInt(split[7]);
                            value = prop.get(key);
                            JSONObject json = new JSONObject(value);
                            TopologyGraphConnection conn = new TopologyGraphConnection(split[1], split[2], split[3], split[4], split[5], split[6], target_instance, json);
                            connections.add(conn);
                        }
                        else
                        {
                            System.err.println("Unknown topology property: " + key + " with value: " + prop.get(key));
                        }
                        break;
                }
            }

            // now that all nodes are here, we can add the requirements to them
            for (String key : reqs.keySet())
            {
                split = key.split(":", 2);
                value = reqs.get(key);
                for (TopologyGraphNode tgn : nodes)
                {
                    if (tgn.getName().equalsIgnoreCase(split[0]))
                    {
                        tgn.addRequirement(split[1], value);
                    }
                }
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException by converting to json: " + e);
        } 
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the rabbit_host
     */
    public String getRabbit_host() {
        return rabbit_host;
    }

    /**
     * @param rabbit_host the rabbit_host to set
     */
    public void setRabbit_host(String rabbit_host) {
        this.rabbit_host = rabbit_host;
    }

    /**
     * @return the rabbit_port
     */
    public int getRabbit_port() {
        return rabbit_port;
    }

    /**
     * @param rabbit_port the rabbit_port to set
     */
    public void setRabbit_port(int rabbit_port) {
        this.rabbit_port = rabbit_port;
    }

    /**
     * @return the rabbit_user
     */
    public String getRabbit_user() {
        return rabbit_user;
    }

    /**
     * @param rabbit_user the rabbit_user to set
     */
    public void setRabbit_user(String rabbit_user) {
        this.rabbit_user = rabbit_user;
    }

    /**
     * @return the rabbit_passwd
     */
    public String getRabbit_passwd() {
        return rabbit_passwd;
    }

    /**
     * @param rabbit_passwd the rabbit_passwd to set
     */
    public void setRabbit_passwd(String rabbit_passwd) {
        this.rabbit_passwd = rabbit_passwd;
    }

    /**
     * @return the mongo_host
     */
    public String getMongo_host() {
        return mongo_host;
    }

    /**
     * @param mongo_host the mongo_host to set
     */
    public void setMongo_host(String mongo_host) {
        this.mongo_host = mongo_host;
    }

    /**
     * @return the mongo_port
     */
    public int getMongo_port() {
        return mongo_port;
    }

    /**
     * @param mongo_port the mongo_port to set
     */
    public void setMongo_port(int mongo_port) {
        this.mongo_port = mongo_port;
    }

    /**
     * @return the mongo_user
     */
    public String getMongo_user() {
        return mongo_user;
    }

    /**
     * @param mongo_user the mongo_user to set
     */
    public void setMongo_user(String mongo_user) {
        this.mongo_user = mongo_user;
    }

    /**
     * @return the mongo_passwd
     */
    public String getMongo_passwd() {
        return mongo_passwd;
    }

    /**
     * @param mongo_passwd the mongo_passwd to set
     */
    public void setMongo_passwd(String mongo_passwd) {
        this.mongo_passwd = mongo_passwd;
    }
    
    public void addNode(TopologyGraphNode n)
    {
        getNodes().add(n);
    }
    
    public void addConnection(TopologyGraphConnection c)
    {
        getConnections().add(c);
    }

    public void loadGraphObject(Object o)
    {
        // set nodes
        // set connections
    }
    
    public Object getGraphObject()
    {
        Object o = null;
        
        // construct o from nodes and connections
        
        
        return o;
    }
    
    @Override
    public String toString()
    {
        return name + " " + description + " " + rabbit_host + " " + rabbit_port + " " + rabbit_user + " " + rabbit_passwd + " " + mongo_host + " " + mongo_port + " " + mongo_user + " " + mongo_passwd;
    }
    
    public JSONObject toJSON()
    {
        JSONObject ret = new JSONObject();
        
        try
        {            
            ret.put("name", name);
            ret.put("description", description);
            
            JSONObject rabbit = new JSONObject();
            rabbit.put("host", rabbit_host);
            rabbit.put("port", rabbit_port);
            rabbit.put("user", rabbit_user);
            rabbit.put("passwd", rabbit_passwd);
            ret.put("rabbit", rabbit);
            
            JSONObject mongo = new JSONObject();
            mongo.put("host", mongo_host);
            mongo.put("port", mongo_port);
            mongo.put("user", mongo_user);
            mongo.put("passwd", mongo_passwd);
            ret.put("mongo", mongo);
            
            if (getNodes() != null)
            {
                JSONArray jarr_nodes = new JSONArray();
                JSONObject jobj_nod;
                for (TopologyGraphNode node : getNodes())
                {
                    jobj_nod = new JSONObject();
                    jobj_nod.put("name", node.getName());
                    jobj_nod.put("instances", node.getInstances());
                    jobj_nod.put("workerspercore", node.getWorkerspercore());
                    jobj_nod.put("requires", node.getRequirements());
                    jarr_nodes.put(jobj_nod);
                }
                ret.put("nodes", jarr_nodes);
            }
            
            if (getConnections() != null)
            {
                JSONArray jarr_connections = new JSONArray();
                JSONObject jobj_conn;
                for (TopologyGraphConnection conn : getConnections())
                {
                    jobj_conn = new JSONObject();
                    jobj_conn.put("source_component", conn.getSource_component());
                    jobj_conn.put("source_worker", conn.getSource_worker());
                    jobj_conn.put("source_endpoint", conn.getSource_endpoint());
                    jobj_conn.put("target_component", conn.getTarget_component());
                    jobj_conn.put("target_worker", conn.getTarget_worker());
                    jobj_conn.put("target_endpoint", conn.getTarget_endpoint());
                    if (conn.getTarget_instance() >= 0)
                        jobj_conn.put("target_instance", conn.getTarget_instance());
                    jobj_conn.put("mapping", conn.getMapping());
                    if (conn.getQueue_name() != null && !conn.getQueue_name().isEmpty())
                        jobj_conn.put("queue_name", conn.getQueue_name());
                    if (conn.getExchange_name() != null && !conn.getExchange_name().isEmpty())
                        jobj_conn.put("exchange_name", conn.getExchange_name());
                    if (conn.getRouting_keys() != null && !conn.getRouting_keys().isEmpty())
                        jobj_conn.put("routing_keys", conn.getRouting_keys());
                    jarr_connections.put(jobj_conn);
                }
                ret.put("connections", jarr_connections);
            }
            
            if (hasGraph_object())
            {
                ret.put("graph_object", graph_object);
            }
            
            if (hasServer_config())
            {
                ret.put("server_config", server_config);
            }
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in toJSON: " + e);
        }
        
        return ret;
    }

    /**
     * @return the nodes
     */
    public ArrayList<TopologyGraphNode> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(ArrayList<TopologyGraphNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the connections
     */
    public ArrayList<TopologyGraphConnection> getConnections() {
        return connections;
    }
    
    public void setConnections(ArrayList<TopologyGraphConnection> connections) {
        this.connections = connections;
    }

    /**
     * @return the graph
     */
    public JSONObject getGraph_object() {
        return graph_object;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph_object(JSONObject graph) {
        this.graph_object = graph;
    }
    
    public boolean hasGraph_object()
    {
        if (graph_object != null)
            return true;
        
        return false;
    }

    /**
     * @return the server_config
     */
    public JSONObject getServer_config() {
        return server_config;
    }

    /**
     * @param server_config the server_config to set
     */
    public void setServer_config(JSONObject server_config) {
        this.server_config = server_config;
    }
    
    public boolean hasServer_config()
    {
        if (server_config != null)
            return true;
        
        return false;
    }
}
