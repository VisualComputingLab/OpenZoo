package gr.iti.openzoo.ui;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public abstract class LoginFilter implements javax.servlet.Filter {
    
    protected ServletContext servletContext;
    
    @Override
    public void init(FilterConfig filterConfig)
    {
        servletContext = filterConfig.getServletContext();
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        resp.setDateHeader("Expires", 0); // Proxies.
        
        if (!isLoggedIn(req))
        {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return; // break filter chain, requested JSP/servlet will not be executed
        }
        
        // propagate to next element in the filter chain, ultimately JSP/servlet gets executed
        chain.doFilter(request, response);
    }

    protected abstract boolean isLoggedIn(HttpServletRequest req);
}
