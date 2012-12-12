package org.xydra.webadmin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.restless.Jetty;
import org.xydra.restless.Restless;
import org.xydra.store.impl.gae.GaeTestfixer;


/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from src/main/webapp. This class is not
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
 * <code>mvn gwt:mergwebxml war:war to update the web.xml in target/iba-1.0.0-SNAPSHOT/WEB-INF
 * 
 * <p>If only static files have been modified, no call is neccesary as this Jetty is configured to load the directly from src/main/webapp.
 * 
 * @author voelkel
 * 
 */
public class RunWebadminJetty {
    
    private static Jetty jetty;
    private static URI uri;
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        GaeTestfixer.enable();
    }
    
    private static final Logger log = LoggerFactory.getLogger(RunWebadminJetty.class);
    
    public static void main(String[] args) throws Exception {
        start();
    }
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        /*
         * Enable tests with GAE (especially mail)
         */
        GaeTestfixer.enable();
    }
    
    public static void start() {
        
        // LogUtils.configureLog4j();
        log.info("--- Booting WebAdmin Jetty ---");
        Restless.DELEGATE_UNHANDLED_TO_DEFAULT = true;
        
        // initialize GAE
        GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
        
        // start jetty
        jetty = new Jetty(8765);
        
        File webappDir = new File("src/main/webapp");
        
        jetty.configure("", webappDir);
        uri = jetty.startServer();
        log.info("Embedded jetty serves " + webappDir.getAbsolutePath() + " at " + uri.toString());
        log.info(".oO ___________ Running ____________________________");
    }
    
    public static URI getServerURI() {
        if(jetty == null) {
            try {
                return new URI("http://0.0.0.0:8765");
            } catch(URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return uri;
    }
    
    public static void stop() {
        assert jetty != null;
        jetty.stopServer();
        log.info("Server stopped.");
    }
}
