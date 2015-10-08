package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooInputConnection;
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import gr.iti.openzoo.impl.OpenZooWorker;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class URLWorker extends OpenZooWorker {

    private OpenZooInputConnection inConn = new OpenZooInputConnection(this, "ep_from");
    private OpenZooOutputConnection outConn = new OpenZooOutputConnection(this, "ep_to");
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
    
    private SimpleDateFormat dateFormatter;
    
    public URLWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- URLWorker()");
        logConn.debug("Created...");
        
        //"Mon Jan 07 12:35:06 +0000 2013"
        dateFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        dateFormatter.setLenient(true);
    }
    
    @Override
    public boolean doWork(Message message) {
                        
        try
        {   
            // either create and insert a new payload:
            //JSONObject json_out = new JSONObject();
            //json_out.put("processed", true);
            //message.setPayload(json_out);
            
            // or work on the existing one:
            JSONObject tweet = message.getPayload();
            
            if (processTweet(tweet))
            {
                JSONObject result = new JSONObject();
                result.put("date_posted", tweet.getLong("date_posted"));
                result.put("images", tweet.getJSONArray("images"));
                message.setPayload(result);
                
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (JSONException e)
        {
            log.error("JSONException: " + e);
            logConn.error("JSONException: " + e);
            return false;
        }
    }
    
    @Override
    public void run()
    {
        log.debug("-- URLWorker.run");
        
        if (!logConn.init())
        {
            log.error("Error by endpoint initialization");
            return;
        }
        
        logConn.debug("-- URLWorker.run");
        
        if (!inConn.init() || !outConn.init())
        {
            log.error("Error by endpoint initialization");
            logConn.error("Error by endpoint initialization");
            return;
        }
        
        log.info("Born!");
        logConn.info("Born!");
        Message message;
        
        while (!enough) 
        {
            message = inConn.getNext();
            
            if (message == null)
            {
                log.error("Received null message, aborting");
                logConn.error("Received null message, aborting");
                break;
            }
            else if (message.isEmpty())
            {
                log.error("Received empty message, discarding");
                logConn.error("Received empty message, discarding");
                inConn.ack(message);
                continue;
            }
            
            boolean success = doWork(message);
            
            if (success)
            {
                message.setSuccess(success);
                outConn.put(message);
            }
            
            inConn.ack(message);
        }

        log.info("Died!");
        logConn.info("Died!");
    }
    
    private Boolean processTweet(JSONObject jobj)
    {
        Date dd;
        String expandedUrl;
        JSONArray urlsjson, mediajson;

        Set<String> shortUrls = new HashSet<>();
        List<String> imageUrls = new ArrayList<>();
            
        //JSONObject objectToBeSent = new JSONObject();
        JSONArray finalUrls;
        
        try
        {
            // extract date_posted
            try
            {
                dd = dateFormatter.parse(jobj.getString("created_at"));
            }
            catch (ParseException ex) 
            {
                log.error("ParseException during parsing creation date in processTweet: " + ex);
                logConn.error("ParseException during parsing creation date in processTweet: " + ex);
                dd = new Date();
            }
            jobj.put("date_posted", dd.getTime());
            
            
            // extract links 1
            if (jobj.getJSONObject("entities").has("urls"))
            {
                urlsjson = jobj.getJSONObject("entities").getJSONArray("urls");
            
                if (urlsjson != null && urlsjson.length() > 0)
                    for (int i = 0; i < urlsjson.length(); i++)
                    {
                        // name expanded_url is misleading
                        shortUrls.add(urlsjson.getJSONObject(i).getString("expanded_url"));
                    }
            }
            
            // extract links 2
            if (jobj.getJSONObject("entities").has("media"))
            {
                mediajson = jobj.getJSONObject("entities").getJSONArray("media");
                
                if (mediajson != null && mediajson.length() > 0)
                {
                    // extract tweet url
                    if (mediajson.optJSONObject(0) != null)
                        jobj.put("tweet_url", mediajson.getJSONObject(0).optString("url", null));
                    
                    for (int i = 0; i < mediajson.length(); i++)
                    {
                        shortUrls.add(mediajson.getJSONObject(i).getString("media_url"));
                    }
                }
            }
            
            // follow and expand urls
            for (String myURL : shortUrls)
            {
                try
                {                            
                    expandedUrl = expandShortURL(myURL);

                    imageUrls.add(expandedUrl);                            
                }
                catch (IOException ex)
                {
                    log.error("IOException during expanding URL: " + myURL + " : " + ex);
                    logConn.error("IOException during expanding URL: " + myURL + " : " + ex);
                }
            }

            if (imageUrls.isEmpty())
                return false;

            if (imageUrls.size() > 0)
            {
                finalUrls = new JSONArray();
                for (String u : imageUrls)
                {
                    finalUrls.put(u);
                }
                jobj.put("images", finalUrls);
            }
        }
        catch (JSONException ex)
        {
            log.error("JSONException during processing JSON object in consumer:" + ex);
            logConn.error("JSONException during processing JSON object in consumer:" + ex);
            return false;
        }
        
        return true;
    }
    
    //TODO: http://www.baeldung.com/unshorten-url-httpclient
    private String expandShortURL(String address) throws IOException 
    {	
        URL url = new URL(address);
		
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY); //using proxy may increase latency
        
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        
        connection.setInstanceFollowRedirects(false);
        connection.addRequestProperty("User-Agent", "Mozilla");
        connection.connect();
        String expandedURL;
        
        expandedURL = connection.getHeaderField("location");
        
        if (expandedURL==null || expandedURL.length()==0)
        {
            URL tmpURL = connection.getURL();
            expandedURL = tmpURL.toString();
        }
      	InputStream myStream = connection.getInputStream();
      	myStream.close();
        
        if (expandedURL==null || expandedURL.length()==0)
        {
            log.error("ERROR: Expanded URL is empty!!!");
            logConn.error("ERROR: Expanded URL is empty!!!");
        }
        
        return expandedURL;
    }
}
