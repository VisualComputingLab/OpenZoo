/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.Message;
import gr.iti.openzoo.impl.OpenZooLoggingConnection;
import gr.iti.openzoo.impl.OpenZooOutputConnection;
import gr.iti.openzoo.impl.OpenZooWorker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author dimitris.samaras
 */
public class InstargamZCrawlerWorker extends OpenZooWorker {

    private OpenZooOutputConnection outConn = new OpenZooOutputConnection(this, "instagram_out");
    private OpenZooLoggingConnection logConn = new OpenZooLoggingConnection(this);
    
    // LOOK OUT FOR "&" AMONG PREFIXES
    private static final String API_SITE = "https://api.instagram.com/v1";
    private static final String PREFIX_TAG = "/tags";
    // https://api.instagram.com/v1/tags/snow/media/recent?access_token=1368132404.f59def8.21ffc415777940d7b5d0123beb4bbacd
    private static final String PREFIX_MEDIA = "/media/recent";
    private static final String PREFIX_NEXT = "max_tag_id=";
    private static final String PREFIX_NEXT_LOC = "max_id=";
    // https://api.instagram.com/v1/tags/search?q=snowy&access_token=1368132404.f59def8.21ffc415777940d7b5d0123beb4bbacd
    private static final String PREFIX_SEARCH = "/search";
    private static final String PREFIX_Q = "q=";
    // DO NOT FORGET THE " ? " AFTER SEARCH!!!
    private static final String API_ACCESS_TOKEN = "access_token=";
    private static final String API_CLIENT_ID = "client_id=";
    // https://api.instagram.com/v1/locations/search?lat=48.858844&lng=2.294351&access_token=1368132404.f59def8.21ffc415777940d7b5d0123beb4bbacd
    private static final String API_LOC_SITE = "https://api.instagram.com/v1/locations";
    private static final String PREFIX_LNG = "lng=";
    private static final String PREFIX_LAT = "lat=";
    //https://api.instagram.com/v1/locations/514276/media/recent?access_token=1368132404.f59def8.21ffc415777940d7b5d0123beb4bbacd
    private static String OP_COMMAND_TAG = "tags";
    private static String OP_COMMAND_LOCATION = "location";

    public InstargamZCrawlerWorker(String threadName) {
        super(threadName);

        log.debug("--InstargamZCrawlerWorker()");
        logConn.debug("Created...");
    }

    @Override
    public boolean doWork(Message message) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }

    @Override
    public void run() {
        log.debug("-- InstargamZCrawlerWorker.run");

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

        // Do your initializing here

        log.info("Born!");
        Message message;


        // Access here the required parameters
        String cliId = getRequiredParameter("clientId");
        String cliSec = getRequiredParameter("clientSecret");
        String operation = getRequiredParameter("search_by");
        String refresh_interval = getRequiredParameter("refresh_interval");
        String lat = getRequiredParameter("latitude");
        String lng = getRequiredParameter("longtitude");
        String topic = getRequiredParameter("keyword").trim().replaceAll(" ", "_");
        //String stop = getRequiredParameter("max_results");
        int interval = 0;
        int stopper = 1000000;

//        if (stop == "" || stop == "_" || stop == null || stop.isEmpty()) {
//            stopper = 1000000;
//        } else {
//            stopper = Integer.parseInt(stop);
//            if (stopper <= 0) {
//                err("Max result has to be between 1 and 1000000, do not use 'max_results' to get all results,");
//                return;
//            }
//        }
        

        while (!enough) {

            try {
                if (operation.equals(OP_COMMAND_TAG)) {
                    
                    if (topic == "" || topic == "_" || topic == null || topic.isEmpty()) {
                        err("No topic given to explore, aborting");
                        return;
                    }

                    URL tagsUrl = null;
                    try {
                        tagsUrl = new URL(API_SITE + PREFIX_TAG + PREFIX_SEARCH + "?" + PREFIX_Q + topic + "&" + API_CLIENT_ID + cliId);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(InstargamZCrawlerWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String tags = callGET(tagsUrl);
                    JSONObject tagsObj = new JSONObject(tags);
                    JSONArray tagsObjData = tagsObj.getJSONArray("data");
                    String tag0 = new JSONObject(tagsObjData.getString(0)).getString("name");
                    if (tag0.equals(topic)) {
                        //THERE IS AN EXACT MATCH
                        int mediacount = 0;
                        try {
                            parseTagMedia(tag0, cliId, stopper, mediacount);
                        } catch (Exception ex) {
                            err("Error retrieving media --tag(0)");
                        }

                    } else {
                        //THERE IS NOT AN EXACT MATCH   
                        int mediacount = 0;
                        for (int i = 0; i < tagsObjData.length(); i++) {
                            //We may get less that 5 tag results...
                            //Consider getting more results i+.... OR consider going only through this loop
                            if (i > 4) {
                                break;
                            }
                            String tag = new JSONObject(tagsObjData.getString(i)).getString("name");
                            int count = 0;
                            try {
                                count = parseTagMedia(tag, cliId, stopper, mediacount);
                            } catch (Exception ex) {
                                err("Error retrieving media --multiple results");
                            }
                            mediacount = count;

                            if (mediacount >= stopper) {
                                break;
                            }
                        }
                    }
                } else if (operation.equals(OP_COMMAND_LOCATION)) {

                    URL tagsUrl = null;
                    try {
                        tagsUrl = new URL(API_LOC_SITE + PREFIX_SEARCH + "?" + PREFIX_LAT + lat + "&" + PREFIX_LNG + lng + "&" + API_CLIENT_ID + cliId);
                    } catch (MalformedURLException ex) {
                        err("searching for loacation --MalformedURLException " + ex);
                    }
                    String tags = callGET(tagsUrl);
                    log("Locations: " + tags);
                    JSONObject tagsObj = new JSONObject(tags);
                    JSONArray tagsObjData = tagsObj.getJSONArray("data");
                    int mediacount = 0;
                    for (int i = 0; i < tagsObjData.length(); i++) {
                        String locid = new JSONObject(tagsObjData.getString(i)).getString("id");
                        int count = 0;
                        try {
                            count = parseLocationMedia(locid, cliId, stopper, mediacount);
                        } catch (Exception ex) {
                            err("Error retrieving mediacount --multiple results");
                        }
                        mediacount = count;
                        if (mediacount >= stopper) {
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                err("JSONException parsing initial response: " + e);
            }

            if (refresh_interval == "" || refresh_interval == null || refresh_interval.isEmpty()) {
                log("Set to run once");
                enough = true;
            } else {
                interval = Integer.parseInt(refresh_interval);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    log.info("Worker woke up");
                    logConn.info("Worker woke up");
                }
            }

        }

        // Do your cleaning here

        log.info("Died!");
        logConn.info("Died!");
    }

    public int parseTagMedia(String tag, String apiKey_val, int stopper, int mediacount) throws Exception {

        String pageToken = "";
        try {
            do {
                URL tagMediaUrl = new URL(API_SITE + PREFIX_TAG + "/" + tag + PREFIX_MEDIA + "?" + API_CLIENT_ID + apiKey_val + "&" + PREFIX_NEXT + pageToken);
                String tagMedia = callGET(tagMediaUrl);
                JSONObject tagMediaobj = new JSONObject(tagMedia);

                pageToken = tagMediaobj.getJSONObject("pagination").optString("next_max_tag_id", "noMore");

                JSONArray tagMediaObjData = tagMediaobj.getJSONArray("data");
                log("media response : " + tagMediaObjData.toString());

                for (int i = 0; i < tagMediaObjData.length(); i++) {
                    JSONObject mediaObject = new JSONObject();
                    JSONObject item = new JSONObject(tagMediaObjData.getString(i));
                    //log(item.toString());
                    if (item.isNull("location")) {
                        mediaObject.put("location", "No location coordinates");
                    } else {
                        mediaObject.put("location", item.getJSONObject("location"));
                    }
                    //From the comments only the comment "text" is needed 
                    JSONArray commentsData = item.getJSONObject("comments").getJSONArray("data");
                    JSONArray comments = new JSONArray();
                    for (int z = 0; z < commentsData.length(); z++) {
                        String com = new JSONObject(commentsData.getString(z)).getString("text");
                        comments.put(com);
                    }
                    mediaObject.put("comments", comments);
                    mediaObject.put("filter", item.getString("filter"));
                    mediaObject.put("created_time", item.getString("created_time"));
                    //convert it to real time?
                    mediaObject.put("likes", item.getJSONObject("likes").getInt("count"));
                    mediaObject.put("image", item.getJSONObject("images").getJSONObject("standard_resolution"));
                    mediaObject.put("users_in_photo", item.getJSONArray("users_in_photo"));
                    if (item.isNull("caption")) {
                        mediaObject.put("caption", "No caption");
                    } else {
                        mediaObject.put("caption", item.getJSONObject("caption").optString("text", "no caption"));
                    }
                    mediaObject.put("id", item.getString("id"));
                    mediaObject.put("user", item.getJSONObject("user"));

                    log(mediaObject.toString());
                    mediacount++;

                    //WRITE to RABBITMQ : 
                    Message message = createEmptyMessage();

                    message.setPayload(mediaObject);

                    //boolean success = doWork(message);

                    message.setSuccess(true);

                    outConn.put(message);
                    //writeToRMQ(mediaObject, qName);

                }
//                5000 reqs/hour minus the initial tags search get... 
//                leaves 1,38 reqs/sec (Thread.sleep(724))     
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) {
                    err("Unexpected exception while sleeping: " + e);
                }

            } while (!pageToken.equals("noMore") && mediacount < stopper);
        } catch (JSONException e) {
            err("Problem in media/search response for tag: " + e);
        }
        return mediacount;
    }

    public int parseLocationMedia(String locid, String apiKey_val, int stopper, int mediacount) throws Exception {

        String pageToken = "";
        try {
            do {
                URL tagMediaUrl = new URL(API_LOC_SITE + "/" + locid + PREFIX_MEDIA + "?" + PREFIX_NEXT_LOC + pageToken + "&" + API_CLIENT_ID + apiKey_val);
                String tagMedia = callGET(tagMediaUrl);
                JSONObject tagMediaobj = new JSONObject(tagMedia);

                //pageToken = tagMediaobj.getJSONObject("pagination").optString("next_max_id", "noMore");
                // becaue next_max_id -->deprecated
                pageToken = tagMediaobj.getJSONObject("pagination").optString("next_max_tag_id ", "noMore");

                JSONArray tagMediaObjData = tagMediaobj.getJSONArray("data");
                log("media response : " + tagMediaObjData.toString());

                for (int i = 0; i < tagMediaObjData.length(); i++) {
                    JSONObject mediaObject = new JSONObject();
                    JSONObject item = new JSONObject(tagMediaObjData.getString(i));
                    //log(item.toString());
                    if (item.isNull("location")) {
                        mediaObject.put("location", "No location coordinates");
                    } else {
                        mediaObject.put("location", item.getJSONObject("location"));
                    }
                    //From the comments only the comment "text" is needed 
                    JSONArray commentsData = item.getJSONObject("comments").getJSONArray("data");
                    JSONArray comments = new JSONArray();
                    for (int z = 0; z < commentsData.length(); z++) {
                        String com = new JSONObject(commentsData.getString(z)).getString("text");
                        comments.put(com);
                    }
                    mediaObject.put("comments", comments);
                    mediaObject.put("filter", item.getString("filter"));
                    mediaObject.put("created_time", item.getString("created_time"));
                    //convert it to real time?
                    mediaObject.put("likes", item.getJSONObject("likes").getInt("count"));
                    mediaObject.put("image", item.getJSONObject("images").getJSONObject("standard_resolution"));
                    mediaObject.put("users_in_photo", item.getJSONArray("users_in_photo"));
                    if (item.isNull("caption")) {
                        mediaObject.put("caption", "No caption");
                    } else {
                        mediaObject.put("caption", item.getJSONObject("caption").optString("text", "no caption"));
                    }

                    mediaObject.put("id", item.getString("id"));
                    mediaObject.put("user", item.getJSONObject("user"));

                    log(mediaObject.toString());
                    mediacount++;
                    //WRITE to RABBITMQ : 
                    Message message = createEmptyMessage();

                    message.setPayload(mediaObject);

                    //boolean success = doWork(message);

                    message.setSuccess(true);

                    outConn.put(message);
                    //writeToRMQ(mediaObject, qName);
                }
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    err("Unexpected exception while sleeping: " + e);
                }
            } while (!pageToken.equals("noMore") && mediacount < stopper);
        } catch (JSONException e) {
            err("Problem in media/search response for tag: " + e);
        }
        return mediacount;
    }

    public String callGET(URL url) {

        String output;
        int code = 0;
        String msg = null;

        try {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            // you need the following if you pass server credentials
            // httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            code = httpCon.getResponseCode();
            msg = httpCon.getResponseMessage();
            //output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;

        } catch (IOException e) {
            output = "IOException during GET CallGET: " + e;
            err(output);
        }
        // Check for Response 
        if ((code != 200 || code != 201) && !("OK".equals(msg))) {
            //output = "NOT OK RESPONSE";
            err("Failed CallGET: HTTP error code : " + code);
        }
        return output;

    }

    private static String convertStreamToString(InputStream is) throws IOException {
        //
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        //
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }

            return writer.toString();
        } else {
            return "";
        }
    }

    private void log(String message) {
        log.info("InstagramCrawler:INFO: " + message);
    }

    private void err(String message) {
        log.error("InstagramCrawler:ERROR: " + message);
    }
}
