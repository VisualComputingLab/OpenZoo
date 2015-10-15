package gr.iti.openzoo.admin;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServiceParameters implements java.io.Serializable {
    
    private ParametersGeneral general = new ParametersGeneral();
    private ParametersMessaging rabbit = new ParametersMessaging();
    private ParametersKeyValue redis = new ParametersKeyValue();
    private ParametersDatabase mongo = new ParametersDatabase();
    //private HashMap<String, ParametersWorker> workers = new HashMap<String, ParametersWorker>();
    //private HashMap<String, QueueParameters> queues = new HashMap<String, QueueParameters>();

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
    public ParametersKeyValue getRedis() {
        return redis;
    }

    /**
     * @param redis the redis to set
     */
    public void setRedis(ParametersKeyValue redis) {
        this.redis = redis;
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
    
    /**
     * @return the queues
     */
//    public HashMap<String, QueueParameters> getQueues() {
//        return queues;
//    }
//
//    /**
//     * @param queues the queues to set
//     */
//    public void setQueues(HashMap<String, QueueParameters> queues) {
//        this.queues = queues;
//    }
    
    @Override
    public String toString()
    {
        String out = "";
        out += general.toString();
        out += rabbit.toString();
        out += redis.toString();
        out += mongo.toString();
        
        return out;
    }

//    /**
//     * @return the workers
//     */
//    public HashMap<String, ParametersWorker> getWorkers() {
//        return workers;
//    }
//
//    /**
//     * @param workers the workers to set
//     */
//    public void setWorkers(HashMap<String, ParametersWorker> workers) {
//        this.workers = workers;
//    }
    
}