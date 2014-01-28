package org.xydra.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.xydra.annotations.CanBeNull;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.Delay;
import org.xydra.restless.utils.HostUtils;


/**
 * This class starts a Jetty server configured to allow testing of the webapp,
 * loading static files directly from src/main/webapp. This class is not
 * required to run the webapp.
 * 
 * This class requires to build and assemble the web app: call
 * <code>mvn clean compile gwt:compile gwt:mergewebxml war:war -Dmaven.test.skip=true -o
 * </code>
 * 
 * <p>
 * If only the client source code changed, use <code>mvn gwt:compile</code>
 * 
 * <p>
 * If the web.xml changed use
 * <code>mvn gwt:mergwebxml war:war to update the web.xml in target/{appname}-SNAPSHOT/WEB-INF
 * 
 * <p>If only static files have been modified, no call is neccesary as this Jetty is configured to load the directly from src/main/webapp.
 * 
 * @author voelkel
 * 
 */
public class Jetty {
    
    private static Logger log = LoggerFactory.getLogger(Jetty.class);
    
    /** exposed in URL */
    private String contextPath;
    
    /**
     * where the webapp is served from. If you use /src/main/webapp you can
     * debug better
     */
    private File docRoot;
    
    private int port;
    
    /** counter for request numbering, helps debugging */
    protected int requests;
    
    @CanBeNull
    private Server server;
    
    /** when was server started or restarted? */
    protected long startTime;
    
    /** User can give a filter to be first in chain. */
    @CanBeNull
    private Filter userFirstFilter;
    
    @CanBeNull
    private WebAppContext webapp;
    
    /**
     * Jetty with default port on 8080
     */
    public Jetty() {
        this(8080);
    }
    
    /**
     * Jetty with your port of choice
     * 
     * @param port
     */
    public Jetty(int port) {
        this.port = port;
    }
    
    /**
     * @param contextPath set to empty string for none
     * @param docRoot
     */
    public void configure(String contextPath, File docRoot) {
        if(this.server != null)
            throw new RuntimeException("server is already startet");
        
        this.contextPath = contextPath;
        this.docRoot = docRoot;
    }
    
    @Deprecated
    public URI startServer(String contextPath, File docRoot) {
        configure(contextPath, docRoot);
        return startServer();
    }
    
    private final Filter imageCachingFilter = new Filter() {
        
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
            
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
                    Locale.US);
            TimeZone tz = TimeZone.getTimeZone("GMT");
            dateFormatter.setTimeZone(tz);
            String rfc1123 = dateFormatter.format(cal.getTime());
            ((HttpServletResponse)response).addHeader("Expires", rfc1123);
            ((HttpServletResponse)response).addHeader("Cache-Control", "public; max-age=31536000");
            filterChain.doFilter(request, responseWrapper);
        }
        
        @Override
        public void init(FilterConfig filterConfig) {
            // do nothing
        }
    };
    
    private final Filter noGwtCachingFilter = new Filter() {
        
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
            
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
                    Locale.US);
            TimeZone tz = TimeZone.getTimeZone("GMT");
            dateFormatter.setTimeZone(tz);
            String rfc1123 = dateFormatter.format(cal.getTime());
            ((HttpServletResponse)response).addHeader("Expires", rfc1123);
            log.debug("Set expire to " + rfc1123);
            ((HttpServletResponse)response).addHeader("Cache-Control", "no-cache, must-revalidate");
            
            // remove IF-MODIFIED-SINCE Header to prevent Jetty from cleverly
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
    
    private final Filter requestCountingFilter = new Filter() {
        
        @Override
        public void destroy() {
            // do nothing
        }
        
        @Override
        public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain)
                throws IOException, ServletException {
            Jetty.this.requests++;
            if(req instanceof HttpServletRequest) {
                HttpServletRequest hreq = (HttpServletRequest)req;
                log.info("_____JETTY #" + Jetty.this.requests + " " + hreq.getMethod() + " "
                        + hreq.getRequestURL() + " @" + timeSinceStart());
            } else {
                log.info("_____JETTY Request Nr. " + Jetty.this.requests + " @" + timeSinceStart());
            }
            filterChain.doFilter(req, response);
        }
        
        @Override
        public void init(FilterConfig filterConfig) {
            // do nothing
        }
    };
    
    private final Filter simulateNetworkDelaysFilter = new Filter() {
        
        @Override
        public void destroy() {
            // do nothing
        }
        
        @Override
        public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain)
                throws IOException, ServletException {
            Delay.servePage();
            filterChain.doFilter(req, response);
        }
        
        @Override
        public void init(FilterConfig filterConfig) {
            // do nothing
        }
    };
    
    /**
     * @return a configured webapp with some nice default servlet filters
     */
    private WebAppContext configureWebapp() {
        WebAppContext webappContext;
        /*
         * Create the webapp. This will load the servlet configuration from
         * docRoot + WEB-INF/web.xml
         */
        webappContext = new WebAppContext(this.docRoot.getAbsolutePath(), this.contextPath);
        
        // make sure 'com.google.appengine.tools.appstats.AppstatsFilter' is on
        // the classpath
        
        // Add the servlets.
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        
        webappContext.setClassLoader(classloader);
        // user given filter goes first
        if(this.userFirstFilter != null) {
            FilterHolder filterHolder = new FilterHolder();
            filterHolder.setFilter(this.userFirstFilter);
            webappContext.addFilter(filterHolder, "*", Handler.ALL);
        }
        
        // caching filter
        {
            FilterHolder filterHolder = new FilterHolder();
            filterHolder.setFilter(this.imageCachingFilter);
            webappContext.addFilter(filterHolder, "*.png", Handler.ALL);
            webappContext.addFilter(filterHolder, "*.gif", Handler.ALL);
            // webappContext.addFilter(filterHolder, ".cache.*", Handler.ALL);
        }
        
        // GWT caching
        FilterHolder gwtFilterHolder = new FilterHolder();
        gwtFilterHolder.setFilter(this.noGwtCachingFilter);
        webappContext.addFilter(gwtFilterHolder, "*.nocache.js", Handler.ALL);
        
        // count requests
        FilterHolder filterHolderForCounting = new FilterHolder();
        filterHolderForCounting.setFilter(this.requestCountingFilter);
        webappContext.addFilter(filterHolderForCounting, "*", Handler.ALL);
        
        // slow down to simulate bad network
        if(Delay.hasServePageDelay()) {
            FilterHolder filterHolderForDelay = new FilterHolder();
            filterHolderForDelay.setFilter(this.simulateNetworkDelaysFilter);
            webappContext.addFilter(filterHolderForDelay, "*", Handler.ALL);
        }
        
        /*
         * Add simple security handler that puts anybody with the name 'admin'
         * into the admin role.
         */
        webappContext.getSecurityHandler().setUserRealm(new UserRealm() {
            
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
        });
        
        // // route requests to static content
        // FilterHolder filterHolderForStaticContent = new FilterHolder();
        // filterHolderForStaticContent.setFilter(new Filter() {
        //
        // @Override
        // public void destroy() {
        // // do nothing
        // }
        //
        // @Override
        // public void doFilter(ServletRequest req, ServletResponse response,
        // FilterChain filterChain) throws IOException, ServletException {
        // if(req instanceof HttpServletRequest) {
        // HttpServletRequest hreq = (HttpServletRequest)req;
        // String path = hreq.getPathInfo();
        // if(path == null) {
        // path = "";
        // }
        // path = path.toLowerCase();
        // if(path.contains("favicon.ico")) {
        // // block
        // log.info("JETTY Blocked: " + hreq.getRequestURL());
        // } else {
        // // don't block
        // filterChain.doFilter(req, response);
        // }
        // } else {
        // filterChain.doFilter(req, response);
        // }
        // }
        //
        // @Override
        // public void init(FilterConfig filterConfig) {
        // // do nothing
        // }
        // });
        // webappContext.addFilter(filterHolderForStaticContent, "*",
        // Handler.ALL);
        return webappContext;
    }
    
    public boolean isConfigured() {
        return this.contextPath != null && this.docRoot != null;
    }
    
    public void setFirstFilter(Filter filter) {
        this.userFirstFilter = filter;
    }
    
    /**
     * Make sure to #configure first
     * 
     * @return the URI where the server runs
     */
    public URI startServer() {
        assert this.server == null;
        if(!isConfigured()) {
            throw new IllegalStateException("configure(...) first");
        }
        
        this.server = new Server(this.port);
        this.webapp = configureWebapp();
        this.server.setHandler(this.webapp);
        
        this.startTime = System.currentTimeMillis();
        try {
            this.server.start();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        
        try {
            return new URI("http://" + HostUtils.getLocalHostname() + ":" + this.port + "/")
                    .resolve(this.contextPath + "/");
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void stopServer() {
        if(this.server != null) {
            try {
                this.server.stop();
                this.server.destroy();
            } catch(Exception e) {
                throw new RuntimeException("error stopping server", e);
            }
            this.server = null;
        }
    }
    
    public long timeSinceStart() {
        return System.currentTimeMillis() - this.startTime;
    }
    
    public boolean isRunning() {
        return this.server.isRunning();
    }
    
}
