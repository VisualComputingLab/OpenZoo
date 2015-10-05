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
        
        String toponame = request.getParameter("topo");
        String level = request.getParameter("level");
        String queuename = toponame + "_logging";
        
        JSONObject json = new JSONObject();
        JSONArray logs = new JSONArray();
        JSONObject message;
        Delivery delivery;
        
        try
        {
            json.put("topo", toponame);
            
            Topology topo = kv.getTopology(toponame);
            if (topo != null)
            {                
                factory.setHost(topo.getRabbit_host());
                factory.setPort(topo.getRabbit_port());
                String usr = topo.getRabbit_user();
                String pwd = topo.getRabbit_passwd();
                if (usr != null && !usr.isEmpty())
                {
                    factory.setUsername(usr);
                    factory.setPassword(pwd);
                }

                connection = factory.newConnection();
                channel = connection.createChannel();     
                channel.basicQos(RABBITMQ_DEFAULT_NUM_MESSAGES);
                qconsumer = new QueueingConsumer(channel);
                
                System.out.println("Connected to rabbitmq for aquiring the logs of topology " + toponame);
                
                Map<String, Object> args = new HashMap<>();
                args.put("x-message-ttl", 5000);
                channel.queueDeclare(queuename, true, false, false, args);
                channel.basicConsume(queuename, false, qconsumer);
                
                for (int i = 0; i < RABBITMQ_DEFAULT_NUM_MESSAGES; i++)
                {
                    delivery = qconsumer.nextDelivery(RABBITMQ_DELIVERY_TIMEOUT);
                    
                    if (delivery == null) break; // queue is probably empty
                    
                    message = new JSONObject(new String(delivery.getBody()));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    if (level == null || level.equalsIgnoreCase(message.getString("type")))
                        logs.put(message);
                }
                
                json.put("response", logs);
            }
        }
        catch (IOException | JSONException e)
        {
            System.err.println("Exception at doGet: " + e);
        }
        catch (InterruptedException ex) 
        {
            System.err.println("InterruptedException during message delivery: " + ex);
        }
        catch (ConsumerCancelledException ex) 
        {
            System.err.println("ConsumerCancelledException during message delivery: " + ex);
        }
        catch (ShutdownSignalException ex) 
        {
        }
        
        response.setContentType("application/json");
        
        try (PrintWriter out = response.getWriter())
        {
            out.println(json);
        }
        
        
        
        //processRequest(request, response);
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
