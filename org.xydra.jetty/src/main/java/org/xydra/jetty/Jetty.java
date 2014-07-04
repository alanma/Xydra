package org.xydra.jetty;

import org.xydra.annotations.CanBeNull;
import org.xydra.conf.IConfig;
import org.xydra.env.Env;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.Delay;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.webapp.WebAppContext;


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
 * <p>If only static files have been modified, no call is necessary as this Jetty is configured to load the directly from src/main/webapp.
 * 
 * @author voelkel
 * 
 */
public class Jetty extends EmbeddedJetty {
    
    private static Logger log = LoggerFactory.getLogger(Jetty.class);
    
    /** User can give a filter to be first in chain. */
    @CanBeNull
    private Filter userFirstFilter;
    
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
     * @deprecated use {@link #configureFromConf(IConfig)} instead
     */
    public Jetty(int port) {
        Env.get().conf().set(ConfParamsJetty.PORT, port);
    }
    
    private final Filter imageCachingFilter = JettyUtils.createOneYearCachingFilter();
    
    private final Filter noGwtCachingFilter = JettyUtils.createNoCacheFilter();
    
    private final Filter requestCountingFilter = JettyUtils.createRequestCountingFilter();
    
    private final Filter simulateNetworkDelaysFilter = JettyUtils
            .createSimulateNetworkDelaysFilter();
    
    /**
     * set some nice default servlet filters
     */
    protected void configureWebapp(WebAppContext webappContext) {
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
        webappContext.getSecurityHandler().setUserRealm(JettyUtils.createInsecureTestUserRealm());
        
        // route requests to static content
        FilterHolder filterHolderForStaticContent = new FilterHolder();
        filterHolderForStaticContent.setFilter(new Filter() {
            
            @Override
            public void destroy() {
                // do nothing
            }
            
            @Override
            public void doFilter(ServletRequest req, ServletResponse response,
                    FilterChain filterChain) throws IOException, ServletException {
                if(req instanceof HttpServletRequest) {
                    HttpServletRequest hreq = (HttpServletRequest)req;
                    String path = hreq.getPathInfo();
                    if(path == null) {
                        path = "";
                    }
                    path = path.toLowerCase();
                    if(path.contains("favicon.ico")) {
                        // block
                        log.info("JETTY Blocked: " + hreq.getRequestURL());
                    } else {
                        // don't block
                        filterChain.doFilter(req, response);
                    }
                } else {
                    filterChain.doFilter(req, response);
                }
            }
            
            @Override
            public void init(FilterConfig filterConfig) {
                // do nothing
            }
        });
        webappContext.addFilter(filterHolderForStaticContent, "*", Handler.ALL);
    }
    
    public void setFirstFilter(Filter filter) {
        this.userFirstFilter = filter;
    }
    
    /**
     * @param contextPath
     * @param docRoot
     * @deprecated use {@link #configureFromConf(IConfig)} instead
     */
    @Deprecated
    public void configure(String contextPath, File docRoot) {
        configure(8888, contextPath, docRoot);
    }
    
}
