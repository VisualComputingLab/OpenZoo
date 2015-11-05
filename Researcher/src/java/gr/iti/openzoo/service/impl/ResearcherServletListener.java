package gr.iti.openzoo.service.impl;

import gr.iti.openzoo.admin.OpenZooContextListener;
import javax.servlet.ServletContextEvent;

/**
 * Web application lifecycle listener.
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ResearcherServletListener extends OpenZooContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        super.contextDestroyed(sce);
    }
}
