package org.xydra.server.test;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.server.IXydraServer;
import org.xydra.server.XydraServerDefaultConfiguration;


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
public class Jetty {
	
	private static Logger log = LoggerFactory.getLogger(Jetty.class);
	
	private int port;
	
	private Server server;
	
	private WebAppContext webapp;
	
	public Jetty() {
		this(8080);
	}
	
	public Jetty(int port) {
		this.port = port;
	}
	
	public URI startServer(String contextPath, File docRoot) {
		
		if(this.server != null)
			throw new RuntimeException("server is already startet");
		
		// Create an instance of the jetty server.
		this.server = new Server(this.port);
		
		/*
		 * Create the webapp. This will load the servlet configuration from
		 * docRoot + WEB-INF/web.xml
		 */
		this.webapp = new WebAppContext(docRoot.getAbsolutePath(), contextPath);
		
		// Add the webapp to the server.
		this.server.setHandler(this.webapp);
		
		try {
			this.server.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			return new URI("http://localhost:" + this.port + "/").resolve(contextPath + "/");
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
	
	public static void main(String[] args) throws Exception {
		
		IXydraServer xydraServer = XydraServerDefaultConfiguration.getInMemoryServer();
		
		// add a default model
		XRepository remoteRepo = xydraServer.getRepository();
		assertFalse(remoteRepo.hasModel(DemoModelUtil.PHONEBOOK_ID));
		DemoModelUtil.addPhonebookModel(remoteRepo);
		
		// allow access to everyone
		XAccessManager arm = xydraServer.getAccessManager();
		arm.setAccess(XA.GROUP_ALL, remoteRepo.getAddress(), XA.ACCESS_READ, true);
		arm.setAccess(XA.GROUP_ALL, remoteRepo.getAddress(), XA.ACCESS_WRITE, true);
		
		// start jetty
		Jetty jetty = new Jetty();
		URI uri = jetty.startServer("/xydra", new File("src/main/webapp"));
		log.info("Started embedded Jetty server. User interface is at " + uri.toString());
		
	}
	
}
