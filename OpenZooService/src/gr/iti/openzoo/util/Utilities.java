package gr.iti.openzoo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Utilities class
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 * @author Dimitris Samaras <dimitris.samaras@iti.gr>
 */
public class Utilities {

    private static final Logger log = LogManager.getLogger(Utilities.class.getName());

    /**
     * Method callGET
     *
     * @param url URL, the url to send the get request
     * @exception IOException When kv directory does not exist (First time web
     * service installed) exception gets generated. Not an issue!
     *
     */
    public String callGET(URL url) {
        String output = null;
        int code = 0;
        String msg = null;
        //InetAddress myip;
        //String myip = "195.251.117.110";

        try {
            //myip = InetAddress.getLocalHost(); // does not provide me the address i want....

            //URL url = new URL("http://"+myip+":8500/v1/catalog/service/"+service);
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
            int codeMe = getFirstDigit(code);
            if (codeMe==4) {
                log.error("IOException during GET (Client error): " + e + ", output: " + code);
            } else if (codeMe==5) {
                log.error("IOException during GET (Server error): " + e + ", output: " + code);
            } else if (codeMe==0) {
                log.info("First of its kind");
                output = "zero";
            }
        }
        log.debug("ds. CallGet code:" + code + " output: " + output + " msg: " + msg);
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

    public String callPut(URL url, String data) throws IOException {
        // Encode the query
        // String encodedQuery = URLEncoder.encode(data, "UTF-8");
        // This is the data that is going to be send to itcuties.com via POST request
        // 'e' parameter contains data to echo
        // String postData = "e=" + encodedQuery +"'";

        // Connect to google.com
        //URL url = new URL("http://echo.itcuties.com");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        //connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
        connection.setRequestProperty("Content-Length", String.valueOf(data.length()));
        // Write data
        OutputStream os = connection.getOutputStream();
        //os.write(postData.getBytes());
        os.write(data.getBytes());
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            responseSB.append(line);
        }

        // Close streams
        br.close();
        os.close();

        return responseSB.toString();
    }

    public String callDELETE(URL url) {
        String output;
        int code = 0;
        String msg = null;
        //InetAddress myip;

        try {
            //myip = InetAddress.getLocalHost(); // does not provide me the address i want....

            //URL url = new URL("http://"+myip+":8500/v1/catalog/service/"+service);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            // you need the following if you pass server credentials
            // httpCon.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("DELETE");
            output = convertStreamToString(httpCon.getInputStream());
            code = httpCon.getResponseCode();
            msg = httpCon.getResponseMessage();
            //output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;

        } catch (IOException e) {
            output = "IOException during DELETE: " + e;
            log.error(output);
        }
        // Check for Response 
        if ((code != 200 || code != 201) && !("OK".equals(msg))) {
            //output = "NOT OK RESPONSE";
            log.error("Failed : HTTP error code : " + code);
            output = Integer.toString(code);
        }
        //System.out.println(output);
        return output;

    }

    public String getHostname() {
        String hostname = "localhost";

        try {
            hostname = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            log.error("UnknownHostException during aquiring hostname: " + ex);
        }

        if (hostname.startsWith("localhost") || hostname.startsWith("127") || hostname.startsWith("0.") || hostname.startsWith("172")) {
            try {
                hostname = new BufferedReader(new InputStreamReader(new URL("http://agentgatech.appspot.com").openStream())).readLine();
            } catch (IOException ex) {
                log.error("IOException during aquiring hostname: " + ex);
            }
        }

        return hostname;
    }

    public int getTomcatPort() {
        int sPort = -1;

        MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
        ObjectName name;
        try {
            name = new ObjectName("Catalina", "type", "Server");
            Server server = (Server) mBeanServer.getAttribute(name, "managedResource");
            //Server server = ServerFactory.getServer();

            Service[] services = server.findServices();
            for (Service service : services) {
                for (Connector connector : service.findConnectors()) {
                    ProtocolHandler protocolHandler = connector.getProtocolHandler();
                    if (protocolHandler instanceof Http11Protocol
                            || protocolHandler instanceof Http11AprProtocol
                            || protocolHandler instanceof Http11NioProtocol) {
                        sPort = connector.getPort();
                        //System.out.println("HTTP Port: " + connector.getPort());
                    }
                }
            }
        } catch (MBeanException ex) {
            log.error("MBeanException during retrieving tomcat port server:" + ex);
        } catch (AttributeNotFoundException ex) {
            log.error("AttributeNotFoundException during retrieving tomcat port server:" + ex);
        } catch (InstanceNotFoundException ex) {
            log.error("InstanceNotFoundException during retrieving tomcat port server:" + ex);
        } catch (ReflectionException ex) {
            log.error("ReflectionException during retrieving tomcat port server:" + ex);
        } catch (MalformedObjectNameException ex) {
            log.error("MalformedObjectNameException during retrieving tomcat port server:" + ex);
        } catch (NullPointerException ex) {
            log.error("NullPointerException during retrieving tomcat port server:" + ex);
        }

        return sPort;
    }

    public JSONObject getJSONFromFile(String filename) {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        JSONObject json = null;

        try {
            in = new BufferedReader(new FileReader(filename));
            String aLine;

            while ((aLine = in.readLine()) != null) {
                sb.append(aLine);
            }
        } catch (IOException e) {
            log.error("IOException during loading properties: " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        try {
            json = new JSONObject(sb.toString());
        } catch (JSONException e) {
            log.error("JSONException during loading properties: " + e);
        }

        return json;
    }

    public void deleteFile(String pathToFile) {
        String result = null;
        try {
            //Delete if tempFile exists
            File fileTemp = new File(pathToFile);
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
            result = "File " + pathToFile + " deleted successfully";
            log.info(result);
        } catch (Exception e) {
            // if any error occurs
            result = "ERROR while deleting file: " + pathToFile + " " + e;
            log.error(result);
        }
    }

    public static int getFirstDigit(int i) {
        while (Math.abs(i) >= 10) {
            i = i / 10;
        }
        return Math.abs(i);
    }
}
