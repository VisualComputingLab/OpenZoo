package gr.iti.openzoo.service.rest;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@javax.ws.rs.ApplicationPath("resources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(gr.iti.openzoo.service.impl.URLUnwrapperCrossOriginResourceSharingFilter.class);
        resources.add(gr.iti.openzoo.service.rest.URLUnwrapperRest.class);
    }
    
}
