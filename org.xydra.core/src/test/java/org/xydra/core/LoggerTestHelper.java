package org.xydra.core;

import org.xydra.log.DefaultLoggerFactorySPI;
import org.xydra.log.LoggerFactory;


/**
 * Helper class to initialize the logger for tests.
 */
public class LoggerTestHelper {
	
	private static boolean initialized;
	
	public static synchronized void init() {
		if(!initialized) {
			LoggerFactory.setLoggerFactorySPI(new DefaultLoggerFactorySPI());
			initialized = true;
		}
	}
	
}
