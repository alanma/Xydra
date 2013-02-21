package org.xydra.gaemyadmin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.util.Log4jUtils;
import org.xydra.restless.Jetty;
import org.xydra.restless.Restless;


/**
 * This class is starts a local Jetty server configured to allow testing of the
 * webapp, loading static files directly from src/main/webapp. This class is not
 * required to run the webapp. Its great for testing.
 * 
 * @author voelkel
 * 
 */
public class RunGaeMyAdminJetty {
	
	private static Jetty jetty;
	private static URI uri;
	
	private static final Logger log = LoggerFactory.getLogger(RunGaeMyAdminJetty.class);
	
	public static void main(String[] args) throws Exception {
		GaeMyAdmin_GaeTestfixer.enable();
		start();
	}
	
	public static void start() {
		Log4jUtils.configureLog4j();
		log.info("--- Booting Favr Jetty ---");
		Restless.DELEGATE_UNHANDLED_TO_DEFAULT = true;
		
		// start jetty
		jetty = new Jetty(8765);
		
		File webappDir = new File("src/main/webapp");
		jetty.configure("", webappDir);
		uri = jetty.startServer();
		log.info("Embedded jetty serves " + webappDir.getAbsolutePath() + " at " + uri.toString());
		log.info(".oO ___________ Running ____________________________");
		System.out.println("Server runs at " + uri.toString());
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