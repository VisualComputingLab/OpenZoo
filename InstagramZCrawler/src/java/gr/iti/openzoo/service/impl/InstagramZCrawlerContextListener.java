/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.openzoo.service.impl;


import gr.iti.openzoo.admin.OpenZooContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Web application lifecycle listener.
 *
 * @author dimitris.samaras
 */
@WebListener()
public class InstagramZCrawlerContextListener extends OpenZooContextListener  {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        super.contextDestroyed(sce);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
