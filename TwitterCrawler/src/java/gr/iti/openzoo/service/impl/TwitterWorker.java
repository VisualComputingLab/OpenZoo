package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
import gr.iti.openzoo.impl.OpenZooLoneWorker;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class TwitterWorker extends OpenZooLoneWorker {

    private OpenZooOutputConnection outConn = new OpenZooOutputConnection(this, "ep_to");
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
    
    public TwitterWorker(String threadName)
    {        
        super(threadName);
        
        log.debug("-- TwitterWorker()");
        logConn.debug("Created...");
    }
    
    @Override
    public boolean doWork(Message message) {
        
        return true;
    }
    
    private Configuration createConfiguration(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret)
    {
        ConfigurationBuilder confBuilder = new ConfigurationBuilder();		
			
        confBuilder.setDebugEnabled(true);
        confBuilder.setOAuthConsumerKey(consumerKey);
        confBuilder.setOAuthConsumerSecret(consumerSecret);
        confBuilder.setOAuthAccessToken(accessToken);
        confBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
        confBuilder.setJSONStoreEnabled(true);
        
        
        return confBuilder.build();
    }
    
    @Override
    public void run()
    {
        log.debug("-- TwitterWorker.run");
        logConn.debug("Running...");
        
        if (!outConn.init())
        {
            log.error("Error by endpoint initialization");
            logConn.error("Error by endpoint initialization");
            return;
        }
        
        // Access here the required parameters
        String conKey = getRequiredParameter("consumerKey");
        String conSec = getRequiredParameter("consumerSecret");
        String accTok = getRequiredParameter("accessToken");
        String accTokSec = getRequiredParameter("accessTokenSecret");
        JSONArray keywords;
        try
        {
            keywords = new JSONArray(getRequiredParameter("keywords"));
        }
        catch (JSONException e)
        {
            log.error("JSONException while reading keywords from KV: " + e);
            logConn.error("JSONException while reading keywords from KV: " + e);
            return;
        }
        
        log.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + keywords);
        logConn.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + keywords);
        
        Configuration config = createConfiguration(conKey, conSec, accTok, accTokSec);
        
        if (keywords.length() == 0)
        {
            log.error("No keywords given, aborting");
            logConn.error("No keywords given, aborting");
            return;
        }

        String allkwordinastring = "";
        String tmps;
        String [] kwords = new String[keywords.length()];
        for (int n = 0; n < keywords.length(); n++)
        {
            tmps = keywords.optString(n);
            kwords[n] = tmps;
            allkwordinastring += tmps + ":";
        }
        
        // create twitter stream and listener

        final String [] kwordarray = kwords;

        StatusListener listener = new StatusListener() 
        {   
            //When a tweet, that contains at least one of the queries, is uploaded:
            @Override
            public void onStatus(Status status) 
            {
                String statusString = DataObjectFactory.getRawJSON(status);

                try 
                {
                    JSONArray jsonarr = new JSONArray();
                    for (int k = 0; k < kwordarray.length; k++)
                    {
                        if (status.getText().toLowerCase().indexOf(kwordarray[k]) >= 0)
                            jsonarr.put(kwordarray[k]);
                    }

                    JSONObject json = new JSONObject(statusString);
                    json.put("keywords", jsonarr);
                    
                    Message message = createEmptyMessage();
                    
                    message.setPayload(json);
            
                    //boolean success = doWork(message);

                    message.setSuccess(true);

                    outConn.put(message);
                    
                } 
                catch (JSONException ex) 
                {
                    log.error("JSONException during sending twitter message to rabbitmq: " + ex);
                    logConn.error("JSONException during sending twitter message to rabbitmq: " + ex);
                } 
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses)
            {
                log.error("onTrackLimitationNotice in StatusListener: numberOfLimitedStatuses = " + numberOfLimitedStatuses);
                logConn.error("onTrackLimitationNotice in StatusListener: numberOfLimitedStatuses = " + numberOfLimitedStatuses);
            }
            @Override
            public void onScrubGeo(long userId, long upToStatusId) {}
            @Override
            public void onException(Exception ex)
            {
                log.error("Exception in StatusListener: " + ex);
                logConn.error("Exception in StatusListener: " + ex);
            }
        };

        log.info("Born!");
        logConn.info("Born!");
        
        TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
        twitterStream.addListener(listener);
        FilterQuery fq = new FilterQuery();
        fq.setIncludeEntities(true);

        fq.track(kwordarray);
        log.info("I will listen for the following keywords: " + allkwordinastring);
        logConn.info("I will listen for the following keywords: " + allkwordinastring);

        twitterStream.filter(fq);
        
                
        while (!enough) 
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                log.info("Worker woke up");
                logConn.info("Worker woke up");
            }
        }
        
        log.info("Cleaning up twitter stream");
        logConn.info("Cleaning up twitter stream");
        twitterStream.cleanUp();
        twitter4j.internal.json.DataObjectFactoryUtil.clearThreadLocalMap();
        twitterStream.shutdown();

        log.info("Died!");
        logConn.info("Died!");
    }

    @Override
    public String publish(JSONObject obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
