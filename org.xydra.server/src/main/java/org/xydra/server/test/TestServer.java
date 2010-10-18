package org.xydra.server.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


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
public class TestServer {
	
	private static Logger log = LoggerFactory.getLogger(TestServer.class);
	
	private int port;
	
	private Server server;
	
	private WebAppContext webapp;
	
	public TestServer() {
		this(8080);
	}
	
	public TestServer(int port) {
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
	
	public IXydraServer getBackend() {
		if(this.webapp == null) {
			throw new RuntimeException("cannot get backend before server is started");
		}
		ServletContext sc = this.webapp.getServletContext();
		return (IXydraServer)sc.getAttribute(XydraRestServer.SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER);
	}
	
	public static void main(String[] args) throws Exception {
		
		// start jetty
		TestServer server = new TestServer();
		URI uri = server.startServer("/xydra", new File("src/main/webapp"));
		
		IXydraServer xydraServer = server.getBackend();
		
		// add a default model
		// TODO move command into transaction
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(xydraServer
		        .getRepositoryAddress(), XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		xydraServer.executeCommand(createCommand, null);
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		xydraServer.executeCommand(tb.build(), null);
		
		// allow access to everyone
		XAddress repoAddr = xydraServer.getRepositoryAddress();
		XAccessManager arm = xydraServer.getAccessManager();
		arm.setAccess(XA.GROUP_ALL, repoAddr, XA.ACCESS_READ, true);
		arm.setAccess(XA.GROUP_ALL, repoAddr, XA.ACCESS_WRITE, true);
		
		log.info("Started embedded Jetty server. User interface is at " + uri.toString());
		
	}
	
}
