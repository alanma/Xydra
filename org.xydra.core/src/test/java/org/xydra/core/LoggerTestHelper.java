package org.xydra.core;

import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;


/**
 * Helper class to initialize the logger for tests.
 */
public class LoggerTestHelper {
    
    private static boolean initialized;
    
    /**
     * Set Log4j logger factory (once)
     */
    public static synchronized void init() {
        if(!initialized) {
            LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
            initialized = true;
        }
    }
    
}
