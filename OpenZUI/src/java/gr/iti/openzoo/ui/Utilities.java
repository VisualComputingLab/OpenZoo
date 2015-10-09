package gr.iti.openzoo.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Utilities class
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 * @author Dimitris Samaras <dimitris.samaras@iti.gr>
 */
public class Utilities {

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
                System.err.println("IOException during GET (Client error): " + e + ", output: " + code);
            } else if (codeMe==5) {
                System.err.println("IOException during GET (Server error): " + e + ", output: " + code);
            } else if (codeMe==0) {
                System.out.println("First of its kind");
                output = "zero";
            }
        }
//        System.out.println("ds. CallGet code:" + code + " output: " + output + " msg: " + msg);
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
        
        StringBuilder responseSB;
        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes());
            responseSB = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseSB.append(line);
                }
            }
        }

        return responseSB.toString();
    }
    
    public String callPOST(URL url, JSONObject object) throws IOException
    {
        try
        {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestProperty("Content-Type", "application/json");
            httpCon.setRequestProperty("Accept", "application/json");
            //httpCon.setRequestProperty("Content-Length", String.valueOf(data.length()));
            httpCon.setRequestMethod("POST");
            
            String objectStr = object.toString();
            try (OutputStreamWriter osw = new OutputStreamWriter(httpCon.getOutputStream())) {
                osw.write(objectStr);
            }
            
            int status = ((HttpURLConnection) httpCon).getResponseCode();
            if (status != 200)
            {
                System.err.println("callPOST returned with status: " + status);
                return null;
            }
            
            String output = convertStreamToString(httpCon.getInputStream());
            return output;
        }
        catch (IOException e)
        {
            System.err.println("callPOST : IOException: " + e);
            return null;
        }
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
            System.err.println(output);
        }
        // Check for Response 
        if ((code != 200 || code != 201) && !("OK".equals(msg))) {
            //output = "NOT OK RESPONSE";
            System.err.println("Failed : HTTP error code : " + code);
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
            System.err.println("UnknownHostException during aquiring hostname: " + ex);
        }

        if (hostname.startsWith("localhost") || hostname.startsWith("127") || hostname.startsWith("0.") || hostname.startsWith("172")) {
            try {
                hostname = new BufferedReader(new InputStreamReader(new URL("http://agentgatech.appspot.com").openStream())).readLine();
            } catch (IOException ex) {
                System.err.println("IOException during aquiring hostname: " + ex);
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
            System.err.println("MBeanException during retrieving tomcat port server:" + ex);
        } catch (AttributeNotFoundException ex) {
            System.err.println("AttributeNotFoundException during retrieving tomcat port server:" + ex);
        } catch (InstanceNotFoundException ex) {
            System.err.println("InstanceNotFoundException during retrieving tomcat port server:" + ex);
        } catch (ReflectionException ex) {
            System.err.println("ReflectionException during retrieving tomcat port server:" + ex);
        } catch (MalformedObjectNameException ex) {
            System.err.println("MalformedObjectNameException during retrieving tomcat port server:" + ex);
        } catch (NullPointerException ex) {
            System.err.println("NullPointerException during retrieving tomcat port server:" + ex);
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
            System.err.println("IOException during loading properties: " + e);
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
            System.err.println("JSONException during loading properties: " + e);
        }

        return json;
    }

    public void deleteFile(String pathToFile) {
        String result;
        try {
            //Delete if tempFile exists
            File fileTemp = new File(pathToFile);
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
            result = "File " + pathToFile + " deleted successfully";
            System.out.println(result);
        } catch (Exception e) {
            // if any error occurs
            result = "ERROR while deleting file: " + pathToFile + " " + e;
            System.err.println(result);
        }
    }

    public static int getFirstDigit(int i) {
        while (Math.abs(i) >= 10) {
            i = i / 10;
        }
        return Math.abs(i);
    }
    
    public static JSONObject readJSONFromWAR(String warPath, String jsonPath)
    {
        JSONObject response = null;
        
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(warPath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null)
            {
                //System.out.println("WAR content: " + entry.getName());

                if (!entry.isDirectory() && entry.getName().equalsIgnoreCase(jsonPath))
                {
                    // extract
                    String content = convertStreamToStringNew(zipIn);
                    response = new JSONObject(content);
                    zipIn.closeEntry();
                    break;
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        catch (IOException | JSONException e)
        {
            System.err.println("Exception at readJSONFromWAR: " + e);
            return null;
        }
        
        return response;
    }
    
    private static String convertStreamToStringNew(java.io.InputStream is)
    {
        Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    public static JSONObject writeJSONToWAR(String warPath, String jsonPath, JSONObject jsonObj)
    {
        JSONObject response = null;
        
        Map<String, String> env = new HashMap<>(); 
        env.put("create", "true");
        Path path = Paths.get(warPath);
        URI uri = URI.create("jar:" + path.toUri());
        try (FileSystem fs = FileSystems.newFileSystem(uri, env))
        {
            Path nf = fs.getPath(jsonPath);
            try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                writer.write(jsonObj.toString(4));
            }
        } catch (IOException | JSONException ex) {
            System.err.println("Got an exception in writeJSONToWAR: " + ex);
        }
        
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(warPath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null)
            {
                //System.out.println("WAR content: " + entry.getName());

                if (!entry.isDirectory() && entry.getName().equalsIgnoreCase(jsonPath))
                {
                    // extract
                    String content = convertStreamToStringNew(zipIn);
                    response = new JSONObject(content);
                    zipIn.closeEntry();
                    break;
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        catch (IOException | JSONException e)
        {
            System.err.println("Exception at readJSONFromWAR: " + e);
            return null;
        }
        
        return response;
    }
    
    // http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
    public static boolean compressDirectory(String path)
    {
        File directoryToZip = new File(path);
        List<File> fileList = new ArrayList<>();
        
        System.out.println("---Getting references to all files in: " + path);
        if (!getAllFiles(directoryToZip, fileList))
            return false;
        
        System.out.println("---Creating zip file");
        if (!writeZipFile(directoryToZip, fileList))
            return false;
        
        System.out.println("---Done");
        
        return true;
    }
    
    public static boolean getAllFiles(File dir, List<File> fileList)
    {
        try
        {
            File[] files = dir.listFiles();
            for (File file : files)
            {
                fileList.add(file);
                if (file.isDirectory())
                {
                    System.out.println("directory:" + file.getCanonicalPath());
                    getAllFiles(file, fileList);
                }
                else
                {
                    System.out.println("     file:" + file.getCanonicalPath());
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }

    public static boolean writeZipFile(File directoryToZip, List<File> fileList)
    {
        try (FileOutputStream fos = new FileOutputStream(directoryToZip.getName() + ".zip"); ZipOutputStream zos = new ZipOutputStream(fos))
        {
            for (File file : fileList)
            {
                if (!file.isDirectory())
                {
                    // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }

    public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
                    IOException
    {
        try (FileInputStream fis = new FileInputStream(file))
        {
            String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                            file.getCanonicalPath().length());
            System.out.println("Writing '" + zipFilePath + "' to zip file");
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0)
            {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
        }
    }
}
