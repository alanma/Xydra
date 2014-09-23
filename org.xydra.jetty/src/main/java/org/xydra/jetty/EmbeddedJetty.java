package org.xydra.jetty;

import org.xydra.annotations.CanBeNull;
import org.xydra.conf.IConfig;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HostUtils;

import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * A nicer way to configure Jetty. Less complex.
 * 
 * Zero-arg constructor.
 * 
 * @author xamde
 */
public abstract class EmbeddedJetty {
    
    /**
     * exposed in user-entered URLs. E.g. a context-path of "/foo" makes all
     * content accessible at "http://example.com/foo/..."
     */
    protected String contextPath;
    
    /**
     * where the webapp is served from. This is the directory that contains
     * /WEB-INF/web.xml or an index.html.
     */
    protected String docRootURL;
    
    @CanBeNull
    private Server server;
    
    private int port;
    
    /**
     * The preferred way to config.
     * 
     * @param conf
     */
    public synchronized void configureFromConf(IConfig conf) {
        assert conf != null;
        this.port = conf.getInt(ConfParamsJetty.PORT);
        this.contextPath = conf.getString(ConfParamsJetty.CONTEXT_PATH);
        this.docRootURL = conf.getString(ConfParamsJetty.DOC_ROOT);
        if(conf.tryToGet(ConfParamsJetty.USE_DEFAULT_SERVLET) == Boolean.TRUE) {
            Restless.DELEGATE_UNHANDLED_TO_DEFAULT = true;
        }
        
        // map Jettys log Xydras
        // org.eclipse.jetty.util.log.Log.setLog(new
        // JettyLog2XydraLogger("initialLogger"));
        try {
            org.eclipse.jetty.util.log.Log.setLog(new org.eclipse.jetty.util.log.Slf4jLog());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        // didn't work
        // System.setProperty("org.eclipse.jetty.util.log.class",
        // JettyLog2XydraLogger.class.getCanonicalName());
        String serverVersion = Server.getVersion();
        log.info("Mapped Jetty logging to global logging. Expect '" + serverVersion
                + "' as next line");
    }
    
    /** when was server started or restarted? */
    protected long startTime;
    
    @CanBeNull
    private WebAppContext webapp;
    
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
        
        // FIXME configurable
        
        // /** HANDLER 1: Serve static content from jar file */
        // ContextHandler staticHandler = new ContextHandler();
        // staticHandler.setContextPath("/s");
        // // FIXME use "./s" ?
        // staticHandler.setResourceBase(".");
        // staticHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        // // resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        
        /**
         * HANDLER 2: Create the webapp. This will load the servlet
         * configuration from docRoot + WEB-INF/web.xml
         */
        
        this.webapp = new WebAppContext();
        
        // ClassLoader loader = this.getClass().getClassLoader();
        // URL autodiscoverResource =
        // loader.getResource(ConfParamsJetty._WEBROOT_MARKER_FILE);
        // if(autodiscoverResource != null) {
        // File markerLoc = new File(autodiscoverResource.getFile());
        // String webrootAutodiscovered =
        // markerLoc.getParentFile().getAbsolutePath();
        // System.out.println("Auto-discovered webroot = " +
        // webrootAutodiscovered);
        // this.webapp.setResourceBase(webrootAutodiscovered);
        // } else {
        // throw new RuntimeException("Auto-conf failed. Place a file called '"
        // + ConfParamsJetty._WEBROOT_MARKER_FILE + "' into your webapp root.");
        // }
        
        this.webapp.setCopyWebDir(false);
        this.webapp.setCopyWebInf(false);
        
        Resource base;
        try {
            base = Resource.newResource(this.docRootURL);
        } catch(MalformedURLException e2) {
            throw new RuntimeException(e2);
        }
        this.webapp.setBaseResource(base);
        this.webapp.setContextPath(this.contextPath);
        
        configureWebapp(this.webapp);
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {
                // staticHandler,
                this.webapp, new DefaultHandler() });
        this.server.setHandler(handlers);
        
        log.debug("Base URI of webapp: " + this.webapp.getBaseResource().getURI());
        
        this.startTime = System.currentTimeMillis();
        try {
            // Setup JMX
            MBeanContainer mbContainer = new MBeanContainer(
                    ManagementFactory.getPlatformMBeanServer());
            this.server.addEventListener(mbContainer);
            this.server.addBean(mbContainer);
            
            this.server.start();
            
            new Thread() {
                
                @Override
                public void run() {
                    while(EmbeddedJetty.this.server.isStarting()) {
                        System.out.println("Starting...");
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException e) {
                        }
                    }
                    System.out.println("Done. Now " + EmbeddedJetty.this.server.getState());
                }
                
            }.start();
            
            // this.server.join();
        } catch(BindException e) {
            log.warn("App is already running at port " + this.port
                    + ", could not automatically stop it.");
            System.exit(1);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        
        try {
            String uriString = "http://" + HostUtils.getLocalHostname() + ":" + this.port + "/";
            return new URI(uriString).resolve(this.contextPath);
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJetty.class);
    
    /**
     * This must be implemented by sub-classes
     * 
     * @param webapp
     */
    protected abstract void configureWebapp(WebAppContext webapp);
    
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
    
    public boolean isRunning() {
        return this.server != null && this.server.isRunning();
    }
    
    public boolean isConfigured() {
        return this.contextPath != null && this.docRootURL != null;
    }
    
}
