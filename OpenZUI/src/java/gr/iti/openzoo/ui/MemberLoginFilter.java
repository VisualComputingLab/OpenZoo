package gr.iti.openzoo.ui;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class MemberLoginFilter extends LoginFilter {

    LoggedInUser myBean = null;
    
    @Override
    protected boolean isLoggedIn(HttpServletRequest req) {
        
        myBean = (LoggedInUser) req.getSession().getAttribute("userBean");
        
//        System.out.println("MaxInactiveInterval in secs: " + req.getSession().getMaxInactiveInterval());
//        System.out.println("LastAccessedTime: " + new Date(req.getSession().getLastAccessedTime()).toString());
        
        if (myBean != null && myBean.isLoggedIn())
            return true;
        
        return false;
    }

    @Override
    public void destroy() {
        if (myBean != null)
            myBean.logOut();
    }
}
