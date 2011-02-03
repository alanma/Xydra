package org.xydra.testgae.xmas;

import java.io.File;
import java.net.URI;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
public class RunJetty {
	
	private static final Logger log = LoggerFactory.getLogger(RunJetty.class);
	
	public static void main(String[] args) throws Exception {
		
		/*
		 * Enable tests with GAE (especially mail)
		 */
		GaeTestfixer.enable();
		// initialize GAE
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// start jetty
		CheckClasspathTool.checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded();
		Jetty jetty = new Jetty();
		
		CheckClasspathTool.checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded();
		URI uri = jetty.startServer("", new File("src/main/webapp"));
		CheckClasspathTool.checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded();
		log.info("Started embedded Jetty server. User interface is at " + uri.toString());
		
	}
}
