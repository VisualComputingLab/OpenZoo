package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
import gr.iti.openzoo.impl.OpenZooLoneWorker;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import java.util.Arrays;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
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

    private static String OP_COMMAND_KEYWORDS = "keywords";
    private static String OP_COMMAND_LOCATION = "location";
    private OpenZooOutputConnection outConn = new OpenZooOutputConnection(this, "tw_output");
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);

    public TwitterWorker(String threadName) {
        super(threadName);

        log.debug("-- TwitterWorker()");
    }

    @Override
    public boolean doWork(Message message) {

        return true;
    }

    private Configuration createConfiguration(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
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
    public void run() {
        log.debug("-- TwitterWorker.run");

        if (!logConn.init()) {
            log.error("Error by endpoint initialization");
            return;
        }

        logConn.debug("Running...");

        if (!outConn.init()) {
            log.error("Error by endpoint initialization");
            logConn.error("Error by endpoint initialization");
            return;
        }

        // Access here the required parameters
        String conKey = getRequiredParameter("consumerKey");
        String conSec = getRequiredParameter("consumerSecret");
        String accTok = getRequiredParameter("accessToken");
        String accTokSec = getRequiredParameter("accessTokenSecret");
        String operation = getRequiredParameter("search_by");

        Configuration config = createConfiguration(conKey, conSec, accTok, accTokSec);
        TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();

        if (operation.equals(OP_COMMAND_KEYWORDS)) {

            JSONArray keywords;

            try {
                keywords = new JSONArray(getRequiredParameter("keywords"));
            } catch (JSONException e) {
                log.error("JSONException while reading keywords from KV: " + e);
                logConn.error("JSONException while reading keywords from KV: " + e);
                return;
            }

            log.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + keywords);
            logConn.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + keywords);

            if (keywords.length() == 0) {
                log.error("No keywords given, aborting");
                logConn.error("No keywords given, aborting");
                return;
            }

            String allkwordinastring = "";
            String tmps;
            String[] kwords = new String[keywords.length()];
            for (int n = 0; n < keywords.length(); n++) {
                tmps = keywords.optString(n);
                kwords[n] = tmps;
                allkwordinastring += tmps + ":";
            }

            // create twitter stream and listener

            final String[] kwordarray = kwords;

            StatusListener listenerKW = new StatusListener() {
                //When a tweet, that contains at least one of the queries, is uploaded:
                @Override
                public void onStatus(Status status) {
                    String statusString = DataObjectFactory.getRawJSON(status);

                    try {
                        JSONArray jsonarr = new JSONArray();
                        for (int k = 0; k < kwordarray.length; k++) {
                            if (status.getText().toLowerCase().indexOf(kwordarray[k]) >= 0) {
                                jsonarr.put(kwordarray[k]);
                            }
                        }

                        JSONObject json = new JSONObject(statusString);
                        json.put("keywords", jsonarr);

                        Message message = createEmptyMessage();

                        message.setPayload(json);

                        //boolean success = doWork(message);

                        message.setSuccess(true);

                        outConn.put(message);

                    } catch (JSONException ex) {
                        log.error("JSONException during sending twitter message to rabbitmq: " + ex);
                        logConn.error("JSONException during sending twitter message to rabbitmq: " + ex);
                    }
                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                }

                @Override
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                    log.error("onTrackLimitationNotice in StatusListener: numberOfLimitedStatuses = " + numberOfLimitedStatuses);
                    logConn.error("onTrackLimitationNotice in StatusListener: numberOfLimitedStatuses = " + numberOfLimitedStatuses);
                }

                @Override
                public void onScrubGeo(long userId, long upToStatusId) {
                }

                @Override
                public void onException(Exception ex) {
                    log.error("Exception in StatusListener: " + ex);
                }
            };

            log.info("Born!");
            logConn.info("Born!");


            twitterStream.addListener(listenerKW);
            FilterQuery fq = new FilterQuery();
            fq.setIncludeEntities(true);

            fq.track(kwordarray);
            log.info("I will listen for the following keywords: " + allkwordinastring);
            logConn.info("I will listen for the following keywords: " + allkwordinastring);

            twitterStream.filter(fq);


        } else if (operation.equals(OP_COMMAND_LOCATION)) {

            double lon1 = Double.parseDouble(getRequiredParameter("lon1"));
            double lat1 = Double.parseDouble(getRequiredParameter("lat1"));
            double lon2 = Double.parseDouble(getRequiredParameter("lon2"));
            double lat2 = Double.parseDouble(getRequiredParameter("lat2"));

            double bbox[][] = {{lon1, lat1}, {lon2, lat2}};

            log.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + Arrays.toString(bbox));
            logConn.info("Read required parameters from KV: " + conKey + " " + conSec + " " + accTok + " " + accTokSec + " " + Arrays.toString(bbox));

            if (bbox == null && bbox.length < 4) {
                log.error("No coordinates given, aborting");
                logConn.error("No coordinates given, aborting");
                return;
            }

            // create twitter stream and listener


            StatusListener listenerLOC = new StatusListener() {
                //When a tweet, that contains at least one of the queries, is uploaded:
                @Override
                public void onStatus(Status status) {
                    String statusString = DataObjectFactory.getRawJSON(status);


                    try {
                        GeoLocation gl = status.getGeoLocation();
                        StringBuilder msg = new StringBuilder();
                        if (gl != null) {
                            msg.append("Lat/Lon: ").append(gl.getLatitude()).append(",").append(gl.getLongitude()).append(" - ");
                            msg.append(status.getText());

                            JSONObject json = new JSONObject(statusString);
                            json.put("location", msg);

                            Message message = createEmptyMessage();

                            message.setPayload(json);

                            //boolean success = doWork(message);

                            message.setSuccess(true);

                            outConn.put(message);
                        }
                    } catch (JSONException ex) {
                        log.error("JSONException during sending twitter message to rabbitmq: " + ex);
                        logConn.error("JSONException during sending twitter message to rabbitmq: " + ex);
                    }
                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                }

                @Override
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                    log.error("onTrackLimitationNotice in StatusListener: numberOfLimitedStatuses = " + numberOfLimitedStatuses);
                    logConn.error("onTrackLimitationNotice in StatusListener: numberOfLimitedStatuses = " + numberOfLimitedStatuses);
                }

                @Override
                public void onScrubGeo(long userId, long upToStatusId) {
                }

                @Override
                public void onException(Exception ex) {
                    log.error("Exception in StatusListener: " + ex);
                }
            };

            log.info("Born!");
            logConn.info("Born!");


            twitterStream.addListener(listenerLOC);
            FilterQuery fq = new FilterQuery();
            fq.setIncludeEntities(true);

            fq.locations(bbox);
            log.info("I will listen for the following location: " + Arrays.toString(bbox));
            logConn.info("I will listen for the following location: " + Arrays.toString(bbox));

            twitterStream.filter(fq);

        }


        while (!enough) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
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
