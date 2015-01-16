package gr.iti.openzoo.admin;

import com.rabbitmq.client.QueueingConsumer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Message {

    protected static Logger log = LogManager.getLogger(Message.class.getName());
    
    private JSONObject message = null;
    private Long deliveryTag = null;
    private String routing_key = null;
    
    public Message(QueueingConsumer.Delivery delivery)
    {
        if (delivery != null)
        {
            try
            {
                message = new JSONObject(new String(delivery.getBody()));
                deliveryTag = delivery.getEnvelope().getDeliveryTag();
                routing_key = delivery.getEnvelope().getRoutingKey();
            }
            catch (JSONException e)
            {
                log.error("JSONException during converting message delivery to json: " + e);
                message = null;
                deliveryTag = delivery.getEnvelope().getDeliveryTag();
            }
        }
    }
    
    public Message(JSONObject json)
    {
        message = json;
    }
    
    public Message()
    {
        message = new JSONObject();
    }
    
    public void addBinaryFile(String key, String path)
    {
        byte[] bytes;
        String encodedBytes;
        
        try
        {
            bytes = getBytesFromFile(new File(path));
            encodedBytes = Base64.encodeBase64String(bytes);
            
            message.put(key, encodedBytes);
        }
        catch (IOException ex) 
        {
            log.error("IOException during reading/encoding binary file: " + ex);
        }
        catch (JSONException ex) 
        {
            log.error("JSONException during reading and encoding binary file: " + ex);
        }
    }
    
    public byte[] getBinaryFile(String key)
    {
        String encodedBytes;
        byte[] bytes = null;
        
        try
        {
            encodedBytes = message.getString(key);
            bytes = Base64.decodeBase64(encodedBytes);
        }
        catch (JSONException ex) 
        {
            log.error("JSONException during decoding binary string: " + ex);
        }
        
        return bytes;
    }

    public static byte[] getBytesFromFile(File file) throws IOException 
    {
        int maxSizeInMBs = 4;
        byte[] bytes;
        
        try (InputStream is = new FileInputStream(file)) 
        {
            long length = file.length();
            if (length > maxSizeInMBs*1024*1024) {
                throw new IOException("File is larger than " + maxSizeInMBs + " MBs");
            }
            bytes = new byte[(int)length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "+file.getName());
            }
        }
        
        return bytes;
    }
    
    /**
     * @return the message
     */
    public String getMessage() {
        return message.toString();
    }
    
    public JSONObject getJSON() {
        
        return message;
    }
    
    public byte[] getBytes() {
        
        return message.toString().getBytes();
    }
    
    public boolean isEmpty()
    {
        return (message == null || message.toString().isEmpty());
    }

    /**
     * @return the deliveryTag
     */
    public Long getDeliveryTag() {
        return deliveryTag;
    }

    /**
     * @return the routing_key
     */
    public String getRouting_key() {
        return routing_key;
    }
    
    public void setRoutingKey(String rk) {
        routing_key = rk;
    }
    
}
