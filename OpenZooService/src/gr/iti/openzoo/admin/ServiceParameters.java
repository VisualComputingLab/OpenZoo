package gr.iti.openzoo.admin;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServiceParameters implements java.io.Serializable {
    
    private ParametersGeneral general = new ParametersGeneral();
    private ParametersMessaging rabbit = new ParametersMessaging();
    private ParametersKeyValue kv = new ParametersKeyValue();
    private ParametersDatabase mongo = new ParametersDatabase();

    /**
     * @return the general
     */
    public ParametersGeneral getGeneral() {
        return general;
    }

    /**
     * @param general the general to set
     */
    public void setGeneral(ParametersGeneral general) {
        this.general = general;
    }

    /**
     * @return the rabbit
     */
    public ParametersMessaging getRabbit() {
        return rabbit;
    }

    /**
     * @param rabbit the rabbit to set
     */
    public void setRabbit(ParametersMessaging rabbit) {
        this.rabbit = rabbit;
    }

    /**
     * @return the redis
     */
    public ParametersKeyValue getKV() {
        return kv;
    }

    /**
     * @param redis the redis to set
     */
    public void setKV(ParametersKeyValue kv) {
        this.kv = kv;
    }

    /**
     * @return the mongo
     */
    public ParametersDatabase getMongo() {
        return mongo;
    }

    /**
     * @param mongo the mongo to set
     */
    public void setMongo(ParametersDatabase mongo) {
        this.mongo = mongo;
    }
        
    @Override
    public String toString()
    {
        String out = "";
        out += general.toString();
        out += rabbit.toString();
        out += kv.toString();
        out += mongo.toString();
        
        return out;
    }
    
}