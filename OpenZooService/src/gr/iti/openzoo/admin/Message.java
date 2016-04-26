package gr.iti.openzoo.admin;

import com.rabbitmq.client.QueueingConsumer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Message {

    protected static Logger log = LogManager.getLogger(Message.class.getName());
    private static Long counter = 1L;
    private final static String hostname = getHostname();
    
    // id unique across the whole workflow
    private String id = null;
    
    // header for various parameters, holds parameter-value pairs
    private JSONObject header = null;
    
    // the actual content
    private JSONObject payload = null;
    
    // logging structure, holds objects with start time, end time and component id
    private JSONArray logging = null;
    
    // queue message id for acknowledging after the end of processing
    private Long deliveryTag = null;
    
    // routing key for forwarding the message to the correct queues after the processing
    private String routing_key = null;
    
    // whether or not the processing was successful
    private boolean success = true;
    
    public Message(QueueingConsumer.Delivery delivery, String cid, int iid, String wid, String eid)
    {        
        if (delivery != null)
        {
            try
            {
                JSONObject message = new JSONObject(new String(delivery.getBody()));
                
                id = message.optString("id", null);
                if (id == null) id = createMessageId();
                
                header = message.optJSONObject("header");
                if (header == null) header = new JSONObject();
                
                payload = message.optJSONObject("payload");
                if (payload == null) payload = message;
                
                logging = message.optJSONArray("log");
                if (logging == null) logging = new JSONArray();
                
                deliveryTag = delivery.getEnvelope().getDeliveryTag();
                routing_key = delivery.getEnvelope().getRoutingKey();
                
                JSONObject newLog = new JSONObject();
                newLog.put("compId", cid);
                newLog.put("instId", iid);
                newLog.put("workId", wid);
                newLog.put("epInId", eid);
                newLog.put("start", System.currentTimeMillis());
                logging.put(newLog);
            }
            catch (JSONException e)
            {
                log.error("JSONException during converting message delivery to json: " + e);
                deliveryTag = delivery.getEnvelope().getDeliveryTag();
            }
        }
    }
    
    private String createMessageId()
    {
        int LENGTH_TIMESTAMP = 8;
        int LENGTH_HOSTNAME = 6;
        int LENGTH_THREADNR = 4;
        int LENGTH_COUNTER = 6;
                
        String ts = Long.toHexString(System.currentTimeMillis() / 1000);
        while (ts.length() < LENGTH_TIMESTAMP)
            ts = "0" + ts;
        String hn = Integer.toHexString(hostname.hashCode());
        if (hn.length() > LENGTH_HOSTNAME)
            hn = hn.substring(0, LENGTH_HOSTNAME);
        while (hn.length() < LENGTH_HOSTNAME)
            hn = "0" + hn;
        String th = Long.toHexString(Thread.currentThread().getId());
        if (th.length() > LENGTH_THREADNR)
            th = th.substring(0, LENGTH_THREADNR);
        while (th.length() < LENGTH_THREADNR)
            th = "0" + th;
        String ct = Long.toHexString(counter++);
        if (ct.length() > LENGTH_COUNTER)
            ct = ct.substring(0, LENGTH_COUNTER);
        while (ct.length() < LENGTH_COUNTER)
            ct = "0" + ct;
                        
        return ts + hn + th + ct;
    }
    
    public Message(JSONObject hdr, JSONObject pld, String cid, int iid, String wid, String einid)
    {
        // create id: Timestamp:hostname:processid:counter
        id = createMessageId();
        
        Long currentTime = System.currentTimeMillis() / 1000;
//        id =    Long.toHexString(currentTime) + ":" + 
//                Long.toHexString(hostname.hashCode()) + ":" + 
//                Long.toHexString(Thread.currentThread().getId()) + ":" +
//                Long.toBinaryString(counter);
        
        
        header = hdr;
        payload = pld;
        logging = new JSONArray();
        JSONObject newLog = new JSONObject();
        try
        {
            newLog.put("compId", cid);
            newLog.put("instId", iid);
            newLog.put("workId", wid);
            if (einid != null && !einid.isEmpty())
                newLog.put("epInId", einid);
            newLog.put("start", currentTime);
            logging.put(newLog);
        }
        catch (JSONException e)
        {
            log.error("JSONException during creating message log: " + e);
        }
    }
    
    public Message(Message copy)
    {
        try
        {
            id = copy.id;
            header = new JSONObject(copy.header.toString());
            payload = new JSONObject(copy.payload.toString());
            logging = new JSONArray(copy.logging.toString());
            deliveryTag = copy.deliveryTag;
            routing_key = copy.routing_key;
            success = copy.success;
        }
        catch (JSONException e)
        {
            log.error("JSONException during copying message: " + e);
        }
    }
    
    public void setProcessingEnd()
    {
        if (logging != null && logging.length() > 0)
        {
            try
            {
                JSONObject lastLog = logging.getJSONObject(logging.length() - 1);
                lastLog.put("end", System.currentTimeMillis());
                lastLog.put("success", success);
            }
            catch (JSONException e)
            {
                log.error("JSONException during setting processing end: " + e);
            }
        }
        else
        {
            log.error("setProcessingEnd called on empty log");
        }
    }
    
    public void setSuccess(boolean succ)
    {
        success = succ;
    }
        
    public void setOutputEndpointId(String epoid)
    {
        if (logging != null && logging.length() > 0)
        {
            try
            {
                JSONObject lastLog = logging.getJSONObject(logging.length() - 1);
                lastLog.put("epOutId", epoid);
            }
            catch (JSONException e)
            {
                log.error("JSONException during setting endpoint out id: " + e);
            }
        }
        else
        {
            log.error("setOutputEndpointId called on empty log");
        }
    }
    
    public void addBinaryFile(String key, String path)
    {
        byte[] bytes;
        String encodedBytes;
        
        try
        {
            bytes = getBytesFromFile(new File(path));
            encodedBytes = Base64.encodeBase64String(bytes);
            
            payload.put(key, encodedBytes);
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
        return getBinaryFile(key, false);
    }
    
    public byte[] getBinaryFile(String key, boolean delete)
    {
        String encodedBytes;
        byte[] bytes = null;
        
        try
        {
            encodedBytes = payload.getString(key);
            bytes = Base64.decodeBase64(encodedBytes);
            
            if (delete) payload.remove(key);
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
    
    public String getID()
    {
        return id;
    }
    
    public JSONObject getHeader() {
        return header;
    }
            
    public JSONObject getPayload() {
        return payload;
    }
    
    public void setPayload(JSONObject pl)
    {
        payload = pl;
    }
    
    public JSONArray getLog() {
        return logging;
    }
    
    public JSONObject getMessageJSON() {
        
        JSONObject message = new JSONObject();
        try
        {
            message.put("id", id);
            message.put("header", header);
            message.put("payload", payload);
            message.put("log", logging);
        }
        catch (JSONException e)
        {
            log.error("JSONException during converting message to byte array: " + e);
        }
        
        return message;
    }
    
    public byte[] getBytes() {
                
        return getMessageJSON().toString().getBytes();
    }
    
    public boolean isEmpty()
    {
        return (payload == null || payload.toString().isEmpty());
    }

    public Long getDeliveryTag() {
        return deliveryTag;
    }

    public String getRouting_key() {
        return routing_key;
    }
    
    public void setRoutingKey(String rk) {
        routing_key = rk;
    }
    
    private static String getHostname()
    {
        String hn;
        try
        {
            hn = InetAddress.getLocalHost().getHostName();
        } 
        catch (UnknownHostException ex)
        {
            hn="unknown";
        }
        
        return hn;
    }
}
