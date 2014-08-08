package org.xydra.jetty;

import org.xydra.annotations.CanBeNull;
import org.xydra.conf.IConfig;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HostUtils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * A nicer way to configure Jetty. Less complex.
 * 
 * Zero-arg constructor.
 * 
 * @author xamde
 */
public abstract class EmbeddedJetty {
    
    /** exposed in URL */
    protected String contextPath;
    
    /**
     * where the webapp is served from. If you use /src/main/webapp you can
     * debug better.
     */
    protected File docRoot;
    
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
        
        Object o = conf.get(ConfParamsJetty.DOC_ROOT);
        File f = null;
        if(o instanceof File) {
            f = (File)o;
        } else if(o instanceof String) {
            String fileName = (String)o;
            f = new File(fileName);
        }
        assert f != null : "could not cast from " + o.getClass();
        this.docRoot = f;
        System.out.println("#### jetty.docRoot = " + this.docRoot);
        if(conf.tryToGet(ConfParamsJetty.USE_DEFAULT_SERVLET) == Boolean.TRUE) {
            Restless.DELEGATE_UNHANDLED_TO_DEFAULT = true;
        }
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
        this.webapp.setResourceBase(".");
        this.webapp.setCopyWebDir(false);
        this.webapp.setCopyWebInf(false);
        
        // this.webapp = new WebAppContext(this.docRoot.getAbsolutePath(),
        // this.contextPath);
        configureWebapp(this.webapp);
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {
        // staticHandler,
        this.webapp
        // , new DefaultHandler()
        });
        this.server.setHandler(handlers);
        // FIXME was this.server.setHandler(this.webapp);
        
        this.startTime = System.currentTimeMillis();
        try {
            // Setup JMX
            MBeanContainer mbContainer = new MBeanContainer(
                    ManagementFactory.getPlatformMBeanServer());
            this.server.addEventListener(mbContainer);
            this.server.addBean(mbContainer);
            
            this.server.start();
            
            // this.server.join();
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
        return this.contextPath != null && this.docRoot != null;
    }
    
}
