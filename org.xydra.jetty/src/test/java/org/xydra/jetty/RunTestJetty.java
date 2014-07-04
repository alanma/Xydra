package org.xydra.jetty;

import org.xydra.jetty.Jetty;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.io.File;
import java.net.URI;


/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from source code. This class is not
 * required to run the webapp.
 * 
 * <p>
 * If only static files have been modified, no call is neccesary as this Jetty
 * is configured to load resources directly from src/test/resources
 * 
 * @author voelkel
 * 
 */
public class RunTestJetty {
    
    private static final Logger log = LoggerFactory.getLogger(RunTestJetty.class);
    
    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        Jetty jetty = new Jetty();
        jetty.configure(8888, "", new File("src/test/resources"));
        URI uri = jetty.startServer();
        log.info("Started embedded Jetty server. User interface is at " + uri.toString());
        
    }
}
