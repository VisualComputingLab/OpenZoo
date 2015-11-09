package gr.iti.openzoo.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import javax.xml.bind.DatatypeConverter;
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
 */
public class Utilities {

    private static final Logger log = LogManager.getLogger(Utilities.class.getName());

    public String callGET(URL url, String usr, String pass)
    {
        String output = null;
        int code = 0;
        String msg = null;
        String servercredentials = null;
        
        if (usr != null && !usr.isEmpty() && pass != null && !pass.isEmpty())
            servercredentials = usr + ":" + pass;

        try
        {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            if (servercredentials != null)
                httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = convertStreamToString(httpCon.getInputStream());
            code = httpCon.getResponseCode();
            msg = httpCon.getResponseMessage();

        }
        catch (IOException e)
        {
            int codeMe = getFirstDigit(code);
            if (codeMe==4)
            {
                System.err.println("IOException during GET (Client error): " + e + ", output: " + code);
            }
            else if (codeMe==5)
            {
                System.err.println("IOException during GET (Server error): " + e + ", output: " + code);
            }
            else if (codeMe==0)
            {
                System.out.println("First of its kind");
                output = "zero";
            }
        }

        return output;
    }
    
    public String callPOST(URL url, String usr, String pass, JSONObject object)
    {
        String output = null;
        int code = 0;
        String servercredentials = null;
        
        if (usr != null && !usr.isEmpty() && pass != null && !pass.isEmpty())
            servercredentials = usr + ":" + pass;

        try 
        {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestProperty("Content-Type", "application/json");
            if (servercredentials != null)
                httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));  // for accessing rabbit rest api
            else
                httpCon.setRequestProperty("Accept", "application/json");   // for accessing own services
            httpCon.setRequestMethod("POST");
            
            String objectStr = object.toString();
            try (OutputStreamWriter osw = new OutputStreamWriter(httpCon.getOutputStream())) {
                osw.write(objectStr);
            }
            
            code = httpCon.getResponseCode();
            output = convertStreamToString(httpCon.getInputStream());
        } 
        catch (IOException e)
        {
            int codeMe = getFirstDigit(code);
            if (codeMe==4)
            {
                System.err.println("IOException during POST (Client error): " + e + ", output: " + code);
            }
            else if (codeMe==5)
            {
                System.err.println("IOException during POST (Server error): " + e + ", output: " + code);
            }
            else if (codeMe==0)
            {
                System.out.println("First of its kind");
                output = "zero";
            }
        }

        return output;
    }
            
    public String callDELETE(URL url, String usr, String pass) 
    {
        String output = null;
        int code = 0;
        String msg = null;
        String servercredentials = null;
        
        if (usr != null && !usr.isEmpty() && pass != null && !pass.isEmpty())
            servercredentials = usr + ":" + pass;

        try
        {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            if (servercredentials != null)
                httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("DELETE");
            output = convertStreamToString(httpCon.getInputStream());
            code = httpCon.getResponseCode();
            msg = httpCon.getResponseMessage();
        }
        catch (IOException e)
        {
            int codeMe = getFirstDigit(code);
            if (codeMe==4)
            {
                System.err.println("IOException during DELETE (Client error): " + e + ", output: " + code);
            }
            else if (codeMe==5)
            {
                System.err.println("IOException during DELETE (Server error): " + e + ", output: " + code);
            }
            else if (codeMe==0)
            {
                System.out.println("First of its kind");
                output = "zero";
            }
        }
        
        return output;
    }

    public String callPUT(URL url, String usr, String pass, String data)
    {
        String output = null;
        int code = 0;
        String servercredentials = null;
        
        if (usr != null && !usr.isEmpty() && pass != null && !pass.isEmpty())
            servercredentials = usr + ":" + pass;

        try
        {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            if (servercredentials != null)
                    httpCon.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(servercredentials.getBytes()));
            httpCon.setRequestMethod("PUT");
            httpCon.setRequestProperty("Content-Type", "application/json");
            //httpCon.setRequestProperty("Accept", "application/json");
            //connection.setRequestProperty("Content-Length", String.valueOf(data.length()));

            StringBuilder responseSB;
            try (OutputStream os = httpCon.getOutputStream()) {
                os.write(data.getBytes());
                responseSB = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(httpCon.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseSB.append(line);
                    }
                }
            }
            output = responseSB.toString();
        }
        catch (IOException e)
        {
            int codeMe = getFirstDigit(code);
            if (codeMe==4)
            {
                System.err.println("IOException during POST (Client error): " + e + ", output: " + code);
            }
            else if (codeMe==5)
            {
                System.err.println("IOException during POST (Server error): " + e + ", output: " + code);
            }
            else if (codeMe==0)
            {
                System.out.println("First of its kind");
                output = "zero";
            }
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
    
    public static int getFirstDigit(int i) {
        while (Math.abs(i) >= 10) {
            i = i / 10;
        }
        return Math.abs(i);
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
}
