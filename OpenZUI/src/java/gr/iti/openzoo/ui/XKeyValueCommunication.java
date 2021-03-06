package gr.iti.openzoo.ui;

import gr.iti.openzoo.pojos.TopologyGraphNode;
import gr.iti.openzoo.pojos.TopologyGraphConnection;
import gr.iti.openzoo.pojos.WarFile;
import gr.iti.openzoo.pojos.Topology;
import gr.iti.openzoo.pojos.Server;
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
public class XKeyValueCommunication {

    private final static int redis_timeout = 5000;
    
    private static JedisPool pool;
    
    String redis_host;
    int redis_port;
    
    public XKeyValueCommunication(String redisHost, int redisPort)
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
    
    public String getKVHost()
    {
        return redis_host;
    }
    
    public int getKVPort()
    {
        return redis_port;
    }
    
    public void stop()
    {
        if (pool != null)
            pool.destroy();
    }
    
    // not used
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
    
    // not used
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
    
    // not used
    public String getValue(String key)
    {
        return getValue(key, false);
    }
    
    // not used
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
        
    // not used
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
    
    // not used
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
    
    // not used
    public String getHashValue(String key, String field)
    {
        return getHashValue(key, field, false);
    }
    
    // not used
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
    
    // not used
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
    
    public MessageStatistics getEndpointStats(String toponame)
    {        
        try (Jedis jedis = pool.getResource())
        {
            MessageStatistics ms = new MessageStatistics();
            
            Set<String> ss = jedis.hkeys("statistics:" + toponame);
            if (ss != null && !ss.isEmpty())
            {
                for (String kk : ss)
                {
                    if (kk.startsWith("endpoint:messages:"))
                    {
                        ms.addEndpointMessages(kk.substring(18), Long.parseLong(jedis.hget("statistics:" + toponame, kk)));
                    }
                    else if (kk.startsWith("endpoint:bytes:"))
                    {
                        ms.addEndpointBytes(kk.substring(15), Long.parseLong(jedis.hget("statistics:" + toponame, kk)));
                    }
                }
                
                return ms;
            }
        }
        catch (Exception ex)
        {
            error("Redis exception (getEndpointStatValues)", ex);
        }
        
        return null;
    }
    
    public HashMap<String, ArrayList<Long>> getEndpointStatValues(String toponame)
    {        
        try (Jedis jedis = pool.getResource())
        {
            HashMap<String, ArrayList<Long>> results = new HashMap<>();
            String key;
            ArrayList<Long> sub;
            
            Set<String> ss = jedis.hkeys("statistics:" + toponame);
            if (ss != null && !ss.isEmpty())
            {
                for (String kk : ss)
                {
                    if (kk.startsWith("endpoint:messages:"))
                    {
                        key = kk.substring(18);
                        sub = results.get(key);
                        if (sub == null)
                        {
                            sub = new ArrayList<>();
                            sub.add(0L);
                            sub.add(0L);
                            results.put(key, sub);
                        }
                        
                        sub.set(0, Long.parseLong(jedis.hget("statistics:" + toponame, kk)));
                    }
                    else if (kk.startsWith("endpoint:bytes:"))
                    {
                        key = kk.substring(15);
                        sub = results.get(key);
                        if (sub == null)
                        {
                            sub = new ArrayList<>();
                            sub.add(0L);
                            sub.add(0L);
                            results.put(key, sub);
                        }
                        
                        sub.set(1, Long.parseLong(jedis.hget("statistics:" + toponame, kk)));
                    }
                }
                
                return results;
            }
        }
        catch (Exception ex)
        {
            error("Redis exception (getEndpointStatValues)", ex);
        }
        
        return null;
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
                jedis.del("statistics:" + toponame);
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
            sb.append(",'threadspercore':");
            sb.append(nod.getThreadspercore());
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
