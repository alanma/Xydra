package org.xydra.core;

import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;


/**
 * Helper class to initialize the logger for tests.
 */
public class LoggerTestHelper {
	
	private static boolean initialized;
	
	public static synchronized void init() {
		if(!initialized) {
			LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
			initialized = true;
		}
	}
	
}
