package org.xydra.jetty;

import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.Delay;


public class JettyUtils {
    private static final Logger log = LoggerFactory.getLogger(JettyUtils.class);
    
    /**
     * @return a {@link UserRealm} in whcih everybody with the name 'admin' is
     *         in the admin role
     */
    public static UserRealm createInsecureTestUserRealm() {
        return new UserRealm() {
            
            @Override
            public Principal authenticate(String username, Object credentials, Request request) {
                return getPrincipal(username);
            }
            
            @Override
            public void disassociate(Principal user) {
            }
            
            @Override
            public String getName() {
                return "dummyRealm";
            }
            
            @Override
            public Principal getPrincipal(final String username) {
                return new Principal() {
                    @Override
                    public String getName() {
                        return username;
                    }
                };
            }
            
            @Override
            public boolean isUserInRole(Principal user, String role) {
                return user.getName().equalsIgnoreCase("admin");
            }
            
            @Override
            public void logout(Principal user) {
            }
            
            @Override
            public Principal popRole(Principal user) {
                return user;
            }
            
            @Override
            public Principal pushRole(Principal user, String role) {
                return user;
            }
            
            @Override
            public boolean reauthenticate(Principal user) {
                return true;
            }
        };
    }
    
    /**
     * Use with caution, it'S very hard to get the resources out of the cache --
     * usually needs clear cache in browser
     * 
     * @return a servlet filter which set response headers for 1 year caching
     */
    public static Filter createOneYearCachingFilter() {
        return new Filter() {
            
            @Override
            public void destroy() {
                // do nothing
            }
            
            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                    FilterChain filterChain) throws IOException, ServletException {
                log.debug("JETTY Image GET " + ((HttpServletRequest)request).getRequestURI());
                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(
                        (HttpServletResponse)response);
                
                // Modify the servlet which serves png and gif files so that
                // it
                // explicitly sets the Pragma, Cache-Control, and Expires
                // headers. The Pragma and Cache-Control headers should be
                // removed. The Expires header should be set according to
                // the
                // caching recommendations mentioned in the previous
                // section.
                
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                
                SimpleDateFormat dateFormatter = new SimpleDateFormat(
                        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                TimeZone tz = TimeZone.getTimeZone("GMT");
                dateFormatter.setTimeZone(tz);
                String rfc1123 = dateFormatter.format(cal.getTime());
                ((HttpServletResponse)response).addHeader("Expires", rfc1123);
                ((HttpServletResponse)response).addHeader("Cache-Control",
                        "public; max-age=31536000");
                filterChain.doFilter(request, responseWrapper);
            }
            
            @Override
            public void init(FilterConfig filterConfig) {
                // do nothing
            }
        };
    }
    
    public static Filter createNoCacheFilter() {
        return new Filter() {
            
            @Override
            public void destroy() {
                // do nothing
            }
            
            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                    FilterChain filterChain) throws IOException, ServletException {
                log.debug("Don't cache " + ((HttpServletRequest)request).getRequestURI());
                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(
                        (HttpServletResponse)response);
                
                // Modify the servlet which serves png and gif files so that it
                // explicitly sets the Pragma, Cache-Control, and Expires
                // headers. The Pragma and Cache-Control headers should be
                // removed. The Expires header should be set according to the
                // caching recommendations mentioned in the previous section.
                
                Calendar cal = Calendar.getInstance();
                // one year in the past
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                
                SimpleDateFormat dateFormatter = new SimpleDateFormat(
                        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                TimeZone tz = TimeZone.getTimeZone("GMT");
                dateFormatter.setTimeZone(tz);
                String rfc1123 = dateFormatter.format(cal.getTime());
                ((HttpServletResponse)response).addHeader("Expires", rfc1123);
                log.debug("Set expire to " + rfc1123);
                ((HttpServletResponse)response).addHeader("Cache-Control",
                        "no-cache, must-revalidate");
                
                // remove IF-MODIFIED-SINCE Header to prevent Jetty from
                // cleverly
                // serving it
                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(
                        (HttpServletRequest)request) {
                    
                    @Override
                    public String getHeader(String name) {
                        if(name.equals(HttpHeaders.IF_MODIFIED_SINCE))
                            return null;
                        else
                            return super.getHeader(name);
                    }
                    
                    @Override
                    public long getDateHeader(String name) {
                        if(name.equals(HttpHeaders.IF_MODIFIED_SINCE))
                            return -1;
                        else
                            return super.getDateHeader(name);
                    }
                    
                };
                
                filterChain.doFilter(requestWrapper, responseWrapper);
            }
            
            @Override
            public void init(FilterConfig filterConfig) {
                // do nothing
            }
        };
    }
    
    public static long timeSinceStart(long startTime) {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * @return a filter which counts requests and logs this as log.info
     */
    public static Filter createRequestCountingFilter() {
        return
        
        new Filter() {
            
            /** counter for request numbering, helps debugging */
            private int requests = 0;
            
            private long startTime = System.currentTimeMillis();
            
            @Override
            public void destroy() {
                // do nothing
            }
            
            @Override
            public void doFilter(ServletRequest req, ServletResponse response,
                    FilterChain filterChain) throws IOException, ServletException {
                this.requests++;
                if(req instanceof HttpServletRequest) {
                    HttpServletRequest hreq = (HttpServletRequest)req;
                    log.info("_____JETTY #" + this.requests + " " + hreq.getMethod() + " "
                            + hreq.getRequestURL() + " @" + timeSinceStart(this.startTime));
                } else {
                    log.info("_____JETTY Request Nr. " + this.requests + " @"
                            + timeSinceStart(this.startTime));
                }
                filterChain.doFilter(req, response);
            }
            
            @Override
            public void init(FilterConfig filterConfig) {
                // do nothing
            }
        };
    }
    
    /**
     * @return a filter which simulates network delays, configure in
     *         {@link Delay}
     */
    public static Filter createSimulateNetworkDelaysFilter() {
        return new Filter() {
            
            @Override
            public void destroy() {
                // do nothing
            }
            
            @Override
            public void doFilter(ServletRequest req, ServletResponse response,
                    FilterChain filterChain) throws IOException, ServletException {
                Delay.servePage();
                filterChain.doFilter(req, response);
            }
            
            @Override
            public void init(FilterConfig filterConfig) {
                // do nothing
            }
        };
    }
}
