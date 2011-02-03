package org.xydra.testgae.xmas;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Small utility to find out if the GAE tools jar is present and the
 * AppstatsFilter can be loaded.
 * 
 * @author voelkel
 */
public class CheckClasspathTool {
	
	private static Logger log = LoggerFactory.getLogger(CheckClasspathTool.class);
	
	static final String NAME = "com.google.appengine.tools.appstats.AppstatsFilter";
	
	public static void main(String[] args) {
		checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded();
	}
	
	/**
	 * Verify that AppstatsFilter class can be loaded.
	 */
	public static void checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded() {
		Class<?> clazz;
		try {
			clazz = Class.forName(NAME);
			log.debug("Loaded " + clazz.getName());
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
}
