package gr.iti.openzoo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class KeyValueCommunication {

    private final static int redis_timeout = 5000;
    
    private Jedis jedis;
    String redis_host;
    int redis_port;
    
    public KeyValueCommunication(String redisHost, int redisPort)
    {
        redis_host = redisHost;
        redis_port = redisPort;
        
        info("Initializing jedis on " + redis_host + ":" + redis_port);
        
        try
        {
            jedis = new Jedis(redis_host, redis_port, redis_timeout);
        }
        catch (Exception ex)
        {
            error("Redis is not accessible: " + ex);
            jedis = null;
        }
    }
    
    public void stop()
    {
        jedis.disconnect();
    }
    
    private void restartJedis()
    {
        info("Restarting Jedis");
        
        try
        {
            jedis.disconnect();
            jedis = new Jedis(redis_host, redis_port, redis_timeout);
        }
        catch (Exception ex)
        {
            error("Redis could not be restarted: " + ex);
            jedis = null;
        }
    }
    
    public String getFirstKeyLike(String key)
    {
        if (jedis != null)
        {
            try
            {
                Set<String> ss = jedis.keys(key);
                if (ss.isEmpty())
                    return null;
                else return (String) (ss.toArray()[0]);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getFirstKeyLike): " + ex);
                return null;
            }
        }
        
        return null;
    }
    
    public String getFirstHashKeyLike(String key, String pattern)
    {
        if (jedis != null)
        {
            try
            {
                Set<String> ss = jedis.hkeys(key);
                if (ss.isEmpty())
                    return null;
                else
                {
                    for (String kk : ss)
                    {
                        if (kk.matches(pattern)) return kk;
                    }
                }
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getFirstHashKeyLike): " + ex);
                return null;
            }
        }
        
        return null;
    }
    
    public String getValue(String key)
    {
        return getValue(key, false);
    }
    
    public String getValue(String key, boolean delete)
    {        
        if (jedis != null)
        {
            try
            {
                String value = jedis.get(key);
                if (delete) jedis.del(key);
                return value;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getValue): " + ex);
                return null;
            }
        }
        else return null;
    }
        
    public void setValue(String key, String value)
    {        
        if (jedis != null)
        {
            try
            {
                jedis.setnx(key, value);
                //jedis.expire(key, 604800);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis exception (setValue): " + ex);
            }
        }
    }
    
    public void incrValue(String key)
    {                
        if (jedis != null)
        {
            try
            {
                jedis.incr(key);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis is not accessible (incrValue): " + ex);
            }
        }
    }
    
    public String getHashValue(String key, String field)
    {
        return getHashValue(key, field, false);
    }
    
    public String getHashValue(String key, String field, boolean delete)
    {
        if (jedis != null)
        {
            try
            {
                String value = jedis.hget(key, field);
                if (delete) jedis.hdel(key, field);
                return value;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getHashValue): " + ex);
                return null;
            }
        }
        else return null;
    }
    
    public void setHashValue(String key, String field, String value)
    {        
        if (jedis != null)
        {
            try
            {
                jedis.hsetnx(key, field, value);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis exception (setValue): " + ex);
            }
        }
    }
    
    public ArrayList<Server> getServers()
    {
        return getServers(false);
    }
    
    public ArrayList<Server> getServers(boolean delete)
    {
        if (jedis != null)
        {
            try
            {
                Set<String> ss = jedis.keys("servers:*");
                ArrayList<Server> allServers = new ArrayList<>();
                for (String srvname : ss)
                {
                    allServers.add(getServer(srvname, delete));
                }
                
                return allServers;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getServers): " + ex);
                return null;
            }
        }
        else return null;
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
        
        if (jedis != null)
        {
            try
            {
                Map<String, String> prop = jedis.hgetAll(key);
                if (delete)
                {
                    jedis.del(key);
//                    for (String s : prop.keySet())
//                        jedis.hdel("servers:" + servername, s);
                }
                
                Server srv = new Server(prop.get("name"), prop.get("address"), Integer.parseInt(prop.get("port")), prop.get("user"), prop.get("passwd"), prop.get("status"));
                
                return srv;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getServer): " + ex);
                return null;
            }
        }
        else return null;
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
        
        if (jedis != null)
        {
            try
            {
                jedis.hmset("servers:" + srv.getName(), prop);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis exception (putServer): " + ex);
            }
        }
    }
    
    public RepositoryParameters getRepositoryParameters()
    {
        return getRepositoryParameters(false);
    }
    
    public RepositoryParameters getRepositoryParameters(boolean delete)
    {        
        String key = "repository";
        
        if (jedis != null)
        {
            try
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
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getRepositoryParameters): " + ex);
                return null;
            }
        }
        else return null;
    }
    
    public void putRepositoryParameters(RepositoryParameters params)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("host", params.getHost());
        prop.put("port", "" + params.getPort());
        prop.put("user", params.getUser());
        prop.put("passwd", params.getPasswd());
        prop.put("path", params.getPath());
        
        if (jedis != null)
        {
            try
            {
                jedis.hmset("repository", prop);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis exception (putRepositoryParameters): " + ex);
            }
        }
    }
    
    public ArrayList<WarFile> getWarFiles()
    {
        return getWarFiles(false);
    }
    
    public ArrayList<WarFile> getWarFiles(boolean delete)
    {
        if (jedis != null)
        {
            try
            {
                Set<String> ss = jedis.keys("warfiles:*");
                ArrayList<WarFile> allWarfiles = new ArrayList<>();
                for (String warname : ss)
                {
                    allWarfiles.add(getWarFile(warname, delete));
                }
                
                return allWarfiles;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getWarFiles): " + ex);
                return null;
            }
        }
        else return null;
    }
    
    public WarFile getWarFile(String warname)
    {
        return getWarFile(warname, false);
    }
    
    public WarFile getWarFile(String warname, boolean delete)
    {
        String key = warname;
        if (!key.startsWith("warfiles:"))
            key = "warfiles:" + warname;
        
        if (jedis != null)
        {
            try
            {
                Map<String, String> prop = jedis.hgetAll(key);
                if (delete)
                {
                    jedis.del(key);
                }
                
                ArrayList<String> reqs = new ArrayList<>();
                String sreq = prop.get("requires");
                if (sreq != null)
                {
                    String[] split = sreq.split(" ");
                    for (int i = 0; i < split.length; i++)
                        reqs.add(split[i]);
                }
                WarFile war = new WarFile(prop.get("filename"), prop.get("folder"), prop.get("version"), prop.get("status"), reqs);
                
                return war;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getWarFile): " + ex);
                return null;
            }
        }
        else return null;
    }
    
    public void putWarFile(WarFile war)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("filename", war.getFilename());
        prop.put("folder", war.getFolder());
        prop.put("version", war.getVersion());
        prop.put("status", war.getStatus());
        ArrayList<String> reqs = war.getRequires();
        String sarr = "";
        for (String ss : reqs)
            sarr += ss + " ";
        prop.put("requires", sarr.trim());
        
        if (jedis != null)
        {
            try
            {
                jedis.hmset("warfiles:" + war.getFilename(), prop);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis exception (putWarFile): " + ex);
            }
        }
    }
    
    public ArrayList<Topology> getTopologies()
    {
        return getTopologies(false);
    }
    
    public ArrayList<Topology> getTopologies(boolean delete)
    {
        if (jedis != null)
        {
            try
            {
                Set<String> ss = jedis.keys("topologies:*");
                ArrayList<Topology> allTopologies = new ArrayList<>();
                for (String topname : ss)
                {
                    allTopologies.add(getTopology(topname, delete));
                }
                
                return allTopologies;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getTopologies): " + ex);
                return null;
            }
        }
        else return null;
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
        
        if (jedis != null)
        {
            try
            {
                Map<String, String> prop = jedis.hgetAll(key);
                if (delete)
                {
                    jedis.del(key);
//                    for (String s : prop.keySet())
//                        jedis.hdel("servers:" + servername, s);
                }
                
                Topology topo = new Topology(prop.get("name"), prop.get("description"), prop.get("rabbit_host"), Integer.parseInt(prop.get("rabbit_port")), prop.get("rabbit_user"), prop.get("rabbit_passwd"), prop.get("mongo_host"), Integer.parseInt(prop.get("mongo_port")), prop.get("mongo_user"), prop.get("mongo_passwd"));
                
                return topo;
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
                return null;
            }
            catch (Exception ex)
            {
                error("Redis exception (getTopology): " + ex);
                return null;
            }
        }
        else return null;
    }
    
    public void putTopology(Topology topo)
    {
        Map<String, String> prop = new HashMap<>();
        
        prop.put("name", topo.getName());
        prop.put("description", topo.getDescription());
        prop.put("rabbit_host", topo.getRabbit_host());
        prop.put("rabbit_port", "" + topo.getRabbit_port());
        prop.put("rabbit_user", topo.getRabbit_user());
        prop.put("rabbit_passwd", topo.getRabbit_passwd());
        prop.put("mongo_host", topo.getMongo_host());
        prop.put("mongo_port", "" + topo.getMongo_port());
        prop.put("mongo_user", topo.getMongo_user());
        prop.put("mongo_passwd", topo.getMongo_passwd());
        
        if (jedis != null)
        {
            try
            {
                jedis.hmset("topologies:" + topo.getName(), prop);
            }
            catch (redis.clients.jedis.exceptions.JedisConnectionException ex)
            {
                restartJedis();
            }
            catch (Exception ex)
            {
                error("Redis exception (putTopology): " + ex);
            }
        }
    }
        
    
    
    
    private void info(String s)
    {
        System.out.println(s);
    }
    
    private void error(String s)
    {
        System.err.println(s);
    }
}
