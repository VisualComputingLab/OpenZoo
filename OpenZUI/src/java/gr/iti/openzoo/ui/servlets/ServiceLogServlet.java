package gr.iti.openzoo.ui.servlets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import gr.iti.openzoo.ui.KeyValueCommunication;
import gr.iti.openzoo.ui.Topology;
import gr.iti.openzoo.ui.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServiceLogServlet extends HttpServlet {
    
    private static int RABBITMQ_DELIVERY_TIMEOUT = 1000;    // msec
    private static int RABBITMQ_DEFAULT_NUM_MESSAGES = 100;
    
    private Utilities util;
    private static KeyValueCommunication kv;
    private JSONObject properties;
    
    private ConnectionFactory factory = new ConnectionFactory();
    private Connection connection;
    protected Channel channel;
    private QueueingConsumer qconsumer;
    
    @Override
    public void init()
    {
        util = (Utilities) getServletContext().getAttribute("util");
        kv = (KeyValueCommunication) getServletContext().getAttribute("kv");
        properties = (JSONObject) getServletContext().getAttribute("properties");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        getLastQueueLogging(request, response);
    }

    public void getLastQueueLogging(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String toponame = request.getParameter("topo");
        String s_level = request.getParameter("level");
        if (s_level == null) s_level = "debug";
        String queuename = toponame + "_logging";
        
        JSONObject json = new JSONObject();
        JSONArray logs = new JSONArray();
        
        Topology topo = kv.getTopology(toponame);
        if (topo != null)
        {
            String url = "http://" + topo.getRabbit_host() + ":15672/api/queues/%2f/";
            util = new Utilities();
            
            try
            {            
                json.put("topo", toponame);
                
                JSONObject pdata = new JSONObject();
                pdata.put("count", 10);
                pdata.put("requeue", false);
                pdata.put("encoding", "auto");
            
                //System.out.println("Querying queue " + queuename);
                String result = util.callPOST(new URL(url + queuename + "/get"), topo.getRabbit_user(), topo.getRabbit_passwd(), pdata);
                
                if (result != null && result.startsWith("["))
                {
                    // parse response and put filtered results into logs
                    JSONArray arr = new JSONArray(result);
                    JSONObject msg;
                    for (int i = 0; i < arr.length(); i++)
                    {
                        msg = new JSONObject(arr.getJSONObject(i).getString("payload"));

                        switch (s_level)
                        {
                            case "debug": logs.put(msg); break;
                            case "info": if (!msg.getString("type").equalsIgnoreCase("debug")) logs.put(msg); break;
                            case "error": if (msg.getString("type").equalsIgnoreCase("error")) logs.put(msg); break;
                        }
                    }
                }
                
                json.put("response", logs);
            }
            catch (IOException | JSONException e)
            {
                System.err.println("Exception at getLastQueueLogging: " + e);
            }
        }
        
        
        response.setContentType("application/json");
        
        try (PrintWriter out = response.getWriter())
        {
            out.println(json);
        }
    }
    
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Service Logs";
    }// </editor-fold>
}
