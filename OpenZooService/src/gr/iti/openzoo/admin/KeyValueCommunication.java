package gr.iti.openzoo.admin;

import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class KeyValueCommunication {

    protected static Logger log = LogManager.getLogger(KeyValueCommunication.class.getName());
    private final static int redis_timeout = 5000;
    
    private Jedis jedis;
    String redis_host;
    int redis_port;
    
    public KeyValueCommunication(String redisHost, int redisPort)
    {
        redis_host = redisHost;
        redis_port = redisPort;
        
        try
        {
            jedis = new Jedis(redis_host, redis_port, redis_timeout);
            
        }
        catch (Exception ex)
        {
            log.error("Redis is not accessible: " + ex);
            jedis = null;
        }
    }
    
    public void stop()
    {
        jedis.disconnect();
    }
    
    private void restartJedis()
    {
        log.info("Restarting Jedis");
        
        try
        {
            jedis.disconnect();
            jedis = new Jedis(redis_host, redis_port, redis_timeout);
        }
        catch (Exception ex)
        {
            log.error("Redis could not be restarted: " + ex);
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
                log.error("Redis exception (getValue): " + ex);
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
                log.error("Redis exception (getValue): " + ex);
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
                log.error("Redis exception (setValue): " + ex);
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
                log.error("Redis is not accessible (incrValue): " + ex);
            }
        }
    }
}
