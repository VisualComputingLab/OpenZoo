package gr.iti.openzoo.admin;

/**
 * Helper class to store initialisation values
 * 
 * @author dimitris.samaras <dimitris.samaras@iti.gr>
 */
public class BasicParameters implements java.io.Serializable{
    
   private String _serviceName = null;
   private String _servicePath = null;
   private String _serviceRest = null;
   private String[] _serviceDescription = new String[3];;
   private String _realPath = null;
   private String _uniqueID = null;
   private String _kv_host = null;
   private Integer _kv_port = -1;
    
   public BasicParameters() {
   }
   
   public String getServiceName(){
      return _serviceName;
   }
   
   public String getServicePath(){
      return _servicePath;
   }
   
   public String[] getServiceDescription(){
      return _serviceDescription;
   }
   
   public String getRealPath(){
      return _realPath;
   }
   
   public String getServiceRest() {
       return _serviceRest;
   }
   
   public String getUUID(){
       return _uniqueID;
   }
   
   public void setServiceName(String serviceName){
      this._serviceName = serviceName;
   }
   
   public void setServicePath(String servicePath){
      this._servicePath = servicePath;
   }
   
   public void setServiceRest(String serviceRest){
      this._serviceRest = serviceRest;
   }
   
   public void setServiceDescription(String[] serviceDescription){
      this._serviceDescription = serviceDescription;
   }
   
   public void setRealPath(String realPath){
      this._realPath= realPath;
   }
   
   public void setUUID(String uniqueUUID){
       this._uniqueID =uniqueUUID;
   }

    public String getKVHost() {
        return _kv_host;
    }

    public void setKVHost(String kv_host) {
        this._kv_host = kv_host;
    }

    public int getKVPort() {
        return _kv_port;
    }

    public void setKVPort(int kv_port) {
        this._kv_port = kv_port;
    }
}