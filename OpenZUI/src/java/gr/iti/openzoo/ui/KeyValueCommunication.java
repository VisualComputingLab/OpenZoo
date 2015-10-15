package gr.iti.openzoo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class KeyValueCommunication {

    private final static int redis_timeout = 5000;
    
    private static JedisPool pool;
    
    String redis_host;
    int redis_port;
    
    public KeyValueCommunication(String redisHost, int redisPort)
    {
        redis_host = redisHost;
        redis_port = redisPort;
        
        info("Initializing jedis on " + redis_host + ":" + redis_port);
        
        try
        {            
            pool = new JedisPool(new JedisPoolConfig(), redis_host, redis_port, redis_timeout);
        }
        catch (Exception ex)
        {
            error("Redis is not accessible", ex);
            pool = null;
        }
    }
    
    public void stop()
    {
        if (pool != null)
            pool.destroy();
    }
    
    public String getFirstKeyLike(String key)
    {        
        try (Jedis jedis = pool.getResource())
        {
            Set<String> ss = jedis.keys(key);
            if (!ss.isEmpty())
                return (String) (ss.toArray()[0]);
        }
        catch (Exception ex)
        {
            error("Redis exception (getFirstKeyLike)", ex);
        }
        
        return null;
    }
    
    public String getFirstHashKeyLike(String key, String pattern)
    {        
        try (Jedis jedis = pool.getResource())
        {
            Set<String> ss = jedis.hkeys(key);
            if (!ss.isEmpty())
            {
                for (String kk : ss)
                {
                    if (kk.matches(pattern)) return kk;
                }
            }
        }
        catch (Exception ex)
        {
            error("Redis exception (getFirstHashKeyLike)", ex);
        }
        
        return null;
    }
    
    public String getValue(String key)
    {
        return getValue(key, false);
    }
    
    public String getValue(String key, boolean delete)
    {        
        try (Jedis jedis = pool.getResource())
        {
            String value = jedis.get(key);
            if (delete) jedis.del(key);
            return value;
        }
        catch (Exception ex)
        {
            error("Redis exception (getValue)", ex);
        }
        
        return null;
    }
        
    public void setValue(String key, String value)
    {        
        try (Jedis jedis = pool.getResource())
        {
            jedis.setnx(key, value);
            //jedis.expire(key, 604800);
        }
        catch (Exception ex)
        {
            error("Redis exception (setValue)", ex);
        }
    }
    
    public void incrValue(String key)
    {                
        try (Jedis jedis = pool.getResource())
        {
            jedis.incr(key);
        }
        catch (Exception ex)
        {
            error("Redis is not accessible (incrValue)", ex);
        }
    }
    
    public String getHashValue(String key, String field)
    {
        return getHashValue(key, field, false);
    }
    
    public String getHashValue(String key, String field, boolean delete)
    {
        try (Jedis jedis = pool.getResource())
        {
            String value = jedis.hget(key, field);
            if (delete) jedis.hdel(key, field);
            return value;
        }
        catch (Exception ex)
        {
            error("Redis exception (getHashValue)", ex);
        }
        
        return null;
    }
    
    public void setHashValue(String key, String field, String value)
    {        
        try (Jedis jedis = pool.getResource())
        {
            jedis.hsetnx(key, field, value);
        }
        catch (Exception ex)
        {
            error("Redis exception (setValue)", ex);
        }
    }
    
    public ArrayList<Server> getServers()
    {
        return getServers(false);
    }
    
    public ArrayList<Server> getServers(boolean delete)
    {
        try (Jedis jedis = pool.getResource())
        {
            Set<String> ss = jedis.keys("servers:*");
            ArrayList<Server> allServers = new ArrayList<>();
            for (String srvname : ss)
            {
                allServers.add(getServer(srvname, delete));
            }
            return allServers;
        }
        catch (Exception ex)
        {
            error("Redis exception (getServers)", ex);
        }
        
        return null;
    }
    
    public Server getServer(String servername)
    {
        return getServer(servername, false);
    }
    
    public Server getServer(String servername, boolean delete)
    {
        String key = servername;
        if (!key.startsWith("servers:"))
            key = "servers:" + servername;
        
        try (Jedis jedis = pool.getResource())
        {
            Map<String, String> prop = jedis.hgetAll(key);
            if (delete)
            {
                jedis.del(key);
//                    for (String s : prop.keySet())
//                        jedis.hdel("servers:" + servername, s);
            }

            Server srv = new Server(prop.get("name"), prop.get("address"), Integer.parseInt(prop.get("port")), prop.get("user"), prop.get("passwd"));

            return srv;
        }
        catch (Exception ex)
        {
            error("Redis exception (getServer)", ex);
        }
        
        return null;
    }
    
    public void putServer(Server srv)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("name", srv.getName());
        prop.put("address", srv.getAddress());
        prop.put("port", "" + srv.getPort());
        prop.put("user", srv.getUser());
        prop.put("passwd", srv.getPasswd());
        prop.put("status", srv.getStatus());
        
        try (Jedis jedis = pool.getResource())
        {
            jedis.del("servers:" + srv.getName());
            jedis.hmset("servers:" + srv.getName(), prop);
        }
        catch (Exception ex)
        {
            error("Redis exception (putServer)", ex);
        }
    }
    
    public RepositoryParameters getRepositoryParameters()
    {
        return getRepositoryParameters(false);
    }
    
    public RepositoryParameters getRepositoryParameters(boolean delete)
    {        
        String key = "repository";
        
        try (Jedis jedis = pool.getResource())
        {
            Map<String, String> prop = jedis.hgetAll(key);

            if (delete)
            {
                jedis.del(key);
            }

            RepositoryParameters params;

            if (prop == null || prop.isEmpty())
                params = new RepositoryParameters("localhost", 21, "anonymous", "", "/");
            else params = new RepositoryParameters(prop.get("host"), Integer.parseInt(prop.get("port")), prop.get("user"), prop.get("passwd"), prop.get("path"));

            return params;
        }
        catch (Exception ex)
        {
            error("Redis exception (getRepositoryParameters)", ex);
        }
        
        return null;
    }
    
    public void putRepositoryParameters(RepositoryParameters params)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("host", params.getHost());
        prop.put("port", "" + params.getPort());
        prop.put("user", params.getUser());
        prop.put("passwd", params.getPasswd());
        prop.put("path", params.getPath());
        
        try (Jedis jedis = pool.getResource())
        {
            jedis.del("repository");
            jedis.hmset("repository", prop);
        }
        catch (Exception ex)
        {
            error("Redis exception (putRepositoryParameters)", ex);
        }
    }
    
    public ArrayList<WarFile> getWarFiles()
    {
        return getWarFiles(false);
    }
    
    public ArrayList<WarFile> getWarFiles(boolean delete)
    {
        try (Jedis jedis = pool.getResource())
        {
            Set<String> ss = jedis.keys("warfiles:*");
            ArrayList<WarFile> allWarfiles = new ArrayList<>();
            for (String warname : ss)
            {
                allWarfiles.add(getWarFile(warname, delete));
            }

            return allWarfiles;
        }
        catch (Exception ex)
        {
            error("Redis exception (getWarFiles)", ex);
        }
        
        return null;
    }
    
    public WarFile getWarFile(String warcompid)
    {
        return getWarFile(warcompid, false);
    }
    
    public WarFile getWarFile(String warcompid, boolean delete)
    {
        String key = warcompid;
        if (!key.startsWith("warfiles:"))
            key = "warfiles:" + warcompid;
        
        try (Jedis jedis = pool.getResource())
        {
            Map<String, String> prop = jedis.hgetAll(key);
            if (delete)
            {
                jedis.del(key);
            }

            //WarFile war = new WarFile(prop.get("component_id"), prop.get("name"), prop.get("service_path"), prop.get("description"), prop.get("filename"), prop.get("folder"), prop.get("version"), prop.get("status"), sreq, swrk);
            WarFile war = new WarFile(prop);

            return war;
        }
        catch (Exception ex)
        {
            error("Redis exception (getWarFile)", ex);
        }
        
        return null;
    }
    
    public void putWarFile(WarFile war)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("component_id", war.getComponent_id());
        prop.put("name", war.getName());
        prop.put("service_path", war.getService_path());
        prop.put("description", war.getDescription());
        prop.put("filename", war.getFilename());
        prop.put("folder", war.getFolder());
        prop.put("version", war.getVersion());
        prop.put("status", war.getStatus());
        prop.put("type", war.getType());
        ArrayList<String> reqs = war.getRequires();
        String sarr = "";
        for (String ss : reqs)
            sarr += ss + " ";
        if (sarr.length() > 0)
            prop.put("requires", sarr.trim());
        
        ArrayList<Worker> works= war.getWorkers();
        String warr = "";
        for (Worker ww : works)
            warr += ww.toString() + " ";
        prop.put("workers", warr.trim());
        
        try (Jedis jedis = pool.getResource())
        {
            jedis.del("warfiles:" + war.getComponent_id());
            jedis.hmset("warfiles:" + war.getComponent_id(), prop);
        }
        catch (Exception ex)
        {
            error("Redis exception (putWarFile)", ex);
        }
    }
    
    public ArrayList<Topology> getTopologies()
    {
        return getTopologies(false);
    }
    
    public ArrayList<Topology> getTopologies(boolean delete)
    {
        try (Jedis jedis = pool.getResource())
        {
            Set<String> ss = jedis.keys("topologies:*");
            ArrayList<Topology> allTopologies = new ArrayList<>();
            for (String topname : ss)
            {
                allTopologies.add(getTopology(topname, delete));
            }

            return allTopologies;
        }
        catch (Exception ex)
        {
            error("Redis exception (getTopologies)", ex);
        }
        
        return null;
    }
    
    public Topology getTopology(String toponame)
    {
        return getTopology(toponame, false);
    }
    
    public Topology getTopology(String toponame, boolean delete)
    {
        String key = toponame;
        if (!key.startsWith("topologies:"))
            key = "topologies:" + toponame;
        
        try (Jedis jedis = pool.getResource())
        {
            Map<String, String> prop = jedis.hgetAll(key);
            
            if (prop.isEmpty())
            {
                return null;
            }
            
            if (delete)
            {
                jedis.del(key);
            }

            Topology topo = new Topology(prop);

            return topo;
        }
        catch (Exception ex)
        {
            error("Redis exception (getTopology), key = " + key + ": ", ex);
        }
        
        return null;
    }
    
    public void putTopology(Topology topo)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("name", topo.getName());
        prop.put("description", topo.getDescription());
        prop.put("rabbit:host", topo.getRabbit_host());
        prop.put("rabbit:port", "" + topo.getRabbit_port());
        prop.put("rabbit:user", topo.getRabbit_user());
        prop.put("rabbit:passwd", topo.getRabbit_passwd());
        prop.put("mongo:host", topo.getMongo_host());
        prop.put("mongo:port", "" + topo.getMongo_port());
        prop.put("mongo:user", topo.getMongo_user());
        prop.put("mongo:passwd", topo.getMongo_passwd());
        
        ArrayList<TopologyGraphNode> nodes = topo.getNodes();
        StringBuilder sb, sb2;
        for (TopologyGraphNode nod : nodes)
        {
            sb = new StringBuilder();
            sb.append("{'name':'");
            sb.append(nod.getName());
            sb.append("','instances':");
            sb.append(nod.getInstances());
            sb.append(",'workerspercore':");
            sb.append(nod.getWorkerspercore());
            sb.append("}");
            prop.put("node:" + nod.getName(), sb.toString());
            
            HashMap<String,String> req = nod.getRequirements();
            for (String key : req.keySet())
                prop.put("requires:" + nod.getName() + ":" + key, req.get(key));
        }
        
        ArrayList<TopologyGraphConnection> connections = topo.getConnections();
        for (TopologyGraphConnection conn : connections)
        {
            sb = new StringBuilder();
            sb.append("{'mapping':");
            sb.append(conn.getMapping());
            if (conn.getQueue_name() != null && !conn.getQueue_name().isEmpty())
            {
                sb.append(",'queue_name':'");
                sb.append(conn.getQueue_name());
                sb.append("'");
            }
            if (conn.getExchange_name() != null && !conn.getExchange_name().isEmpty())
            {
                sb.append(",'exchange_name':'");
                sb.append(conn.getExchange_name());
                sb.append("'");
            }
            if (conn.getRouting_keys() != null && !conn.getRouting_keys().isEmpty())
            {
                sb.append(",'routing_keys':");
                sb.append(conn.getRouting_keys_json());
            }
            sb.append("}");
            
            sb2 = new StringBuilder();
            sb2.append("connection:");
            sb2.append(conn.getSource_component());
            sb2.append(":");
            sb2.append(conn.getSource_worker());
            sb2.append(":");
            sb2.append(conn.getSource_endpoint());
            sb2.append(":");
            sb2.append(conn.getTarget_component());
            sb2.append(":");
            sb2.append(conn.getTarget_worker());
            sb2.append(":");
            sb2.append(conn.getTarget_endpoint());
            if (conn.getTarget_instance() >= 0)
            {
                sb2.append(":");
                sb2.append(conn.getTarget_instance());
            }
            prop.put(sb2.toString(), sb.toString());
        }
        
        if (topo.hasGraph_object())
            prop.put("graph_object", topo.getGraph_object().toString());
        
        if (topo.hasConf_object())
            prop.put("conf_object", topo.getConf_object().toString());
        
        try (Jedis jedis = pool.getResource())
        {
            jedis.del("topologies:" + topo.getName());
            jedis.hmset("topologies:" + topo.getName(), prop);
        }
        catch (Exception ex)
        {
            error("Redis exception (putTopology): ", ex);
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
