package org.xydra.jetty;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.xydra.annotations.CanBeNull;
import org.xydra.conf.IConfig;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HostUtils;


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
     * debug better
     */
    protected File docRoot;
    
    @CanBeNull
    private Server server;
    
    private int port;
    
    /**
     * @param port
     * @param contextPath set to empty string for none
     * @param docRoot
     * 
     * @deprecated use {@link #configureFromConf(IConfig)} instead
     */
    @Deprecated
    public synchronized void configure(int port, String contextPath, File docRoot) {
        if(isRunning())
            throw new RuntimeException("server is already startet");
        
        this.port = port;
        this.contextPath = contextPath;
        this.docRoot = docRoot;
    }
    
    /**
     * The preferred way to config.
     * 
     * @param conf
     */
    public synchronized void configureFromConf(IConfig conf) {
        assert conf != null;
        this.port = conf.getInt(ConfParamsJetty.PORT);
        this.contextPath = conf.getString(ConfParamsJetty.CONTEXT_PATH);
        this.docRoot = new File(conf.getString(ConfParamsJetty.DOC_ROOT));
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
        /*
         * Create the webapp. This will load the servlet configuration from
         * docRoot + WEB-INF/web.xml
         */
        this.webapp = new WebAppContext(this.docRoot.getAbsolutePath(), this.contextPath);
        
        configureWebapp(this.webapp);
        
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
