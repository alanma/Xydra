package org.xydra.xgae.gaeutils;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Small utility to find out if the GAE tools jar is present and the
 * AppstatsFilter can be loaded.
 * 
 * There were some issues in former times with that.
 * 
 * @author voelkel
 */
public class CheckClasspathTool {

	private static Logger log = LoggerFactory.getLogger(CheckClasspathTool.class);

	static final String NAME = "com.google.appengine.tools.appstats.AppstatsFilter";

	/**
	 * Verify that AppstatsFilter class can be loaded.
	 */
	public static void checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded() {
		Class<?> clazz;
		try {
			clazz = Class.forName(NAME);
			log.debug("Loaded " + clazz.getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		checkGaeToolsJarIsPresentAndAppstatsFilterClassCanBeLoaded();
	}

}
