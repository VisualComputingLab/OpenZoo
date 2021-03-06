package gr.iti.openzoo.admin;

import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class XKeyValueCommunication {

    protected static Logger log = LogManager.getLogger(XKeyValueCommunication.class.getName());
    private final static int redis_timeout = 5000;
    
    private JedisPool pool;
    String redis_host;
    int redis_port;
    
    public XKeyValueCommunication(String redisHost, int redisPort)
    {
        redis_host = redisHost;
        redis_port = redisPort;
        
        try
        {
            pool = new JedisPool(new JedisPoolConfig(), redis_host, redis_port, redis_timeout);
        }
        catch (Exception ex)
        {
            log.error("Redis is not accessible: " + ex);
            pool = null;
        }
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
            log.error("Redis exception (getFirstKeyLike): " + ex);
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
            log.error("Redis exception (getFirstHashKeyLike): " + ex);
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
            log.error("Redis exception (getValue): " + ex);
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
            log.error("Redis exception (setValue): " + ex);
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
            log.error("Redis is not accessible (incrValue): " + ex);
        }
    }
    
    public void incrHashValue(String key, String field, int i)
    {                
        try (Jedis jedis = pool.getResource())
        {
            jedis.hincrBy(key, field, i);
        }
        catch (Exception ex)
        {
            log.error("Redis is not accessible (incrValue): " + ex);
        }
    }
    
    public void delHashValue(String key, String field)
    {                
        try (Jedis jedis = pool.getResource())
        {
            jedis.hdel(key, field);
        }
        catch (Exception ex)
        {
            log.error("Redis is not accessible (incrValue): " + ex);
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
            log.error("Redis exception (getHashValue): " + ex);
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
            log.error("Redis exception (setValue): " + ex);
        }
    }
}
