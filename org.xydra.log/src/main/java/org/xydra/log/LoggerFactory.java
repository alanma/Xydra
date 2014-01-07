package org.xydra.log;

import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.ThreadSafe;

import com.google.gwt.core.client.GWT;


/**
 * The basic logger factory. Same signature to obtain a {@link Logger} as slf4j.
 * 
 * @author voelkel
 * 
 */
@ThreadSafe
public class LoggerFactory {
    
    private static ILoggerFactorySPI loggerFactorySPI;
    private static Set<ILogListener> logListeners_ = new HashSet<ILogListener>();
    
    public static final String ROOT_LOGGER_NAME = "org.xydra.log.system";
    
    /**
     * All log messages are also sent to a registered {@link ILogListener}.
     * 
     * @param logListener a listener to receive all log messages
     */
    public static synchronized void addLogListener(ILogListener logListener) {
        ensureLoggerFactoryDefined();
        logListeners_.add(logListener);
        Logger logger = loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null);
        assert logger != null;
        logger.info("Logging: Attached log listener " + logListener.getClass().getName());
    }
    
    public static synchronized void removeLogListener(ILogListener logListener) {
        ensureLoggerFactoryDefined();
        logListeners_.remove(logListener);
        loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(
                "Logging: Removed log listener " + logListener.getClass().getName());
    }
    
    /**
     * @param clazz used as a name for the logger
     * @return a logger, using the sub-system configured by
     */
    public static synchronized Logger getLogger(Class<?> clazz) {
        ensureLoggerFactoryDefined();
        return loggerFactorySPI.getLogger(clazz.getName(), logListeners_);
    }
    
    public static synchronized Logger getThreadSafeLogger(Class<?> clazz) {
        ensureLoggerFactoryDefined();
        return loggerFactorySPI.getThreadSafeLogger(clazz.getName(), logListeners_);
    }
    
    private static synchronized void ensureLoggerFactoryDefined() {
        if(loggerFactorySPI == null) {
            setFallbackLoggerFactory();
            
            try {
                throw new RuntimeException("MARKER (This is NOT an error)");
            } catch(RuntimeException e) {
                loggerFactorySPI.getLogger(LoggerFactory.ROOT_LOGGER_NAME, null).info(
                        "LoggerFactory.init() triggered by ", e);
            }
        }
    }
    
    private static synchronized void setFallbackLoggerFactory() {
        
        // try to use GWT logger
        if(gwtEnabled() && gwtLogEnabled()) {
            loggerFactorySPI = new GwtLoggerFactorySPI();
            loggerFactorySPI.getLogger(LoggerFactory.ROOT_LOGGER_NAME, null).info(
                    "Logging: Found no LoggerFactorySPI, using GWT Log");
        } else {
            loggerFactorySPI = new DefaultLoggerFactorySPI();
            loggerFactorySPI.getLogger(LoggerFactory.ROOT_LOGGER_NAME, null).error(
                    "Logging: Found no LoggerFactorySPI, using default to std.out");
            try {
                throw new RuntimeException(
                        "Logging: FYI (Not an error) This was the first logging call which triggered the log init(). ");
            } catch(RuntimeException e) {
                loggerFactorySPI.getLogger(LoggerFactory.ROOT_LOGGER_NAME, null).info(
                        "Logging: Printing caller to System.out", e);
                e.fillInStackTrace();
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @return true if this code is running as JavaScript (or in JUnit tests)
     */
    public static boolean gwtEnabled() {
        try {
            if(GWT.isClient()) {
                return true;
            }
        } catch(Exception e) {
        } catch(Error e) {
        }
        return false;
    }
    
    /**
     * @return true if the GWT Log system is working
     */
    public static boolean gwtLogEnabled() {
        try {
            // any class access would do
            if(com.google.gwt.logging.client.NullLogHandler.class != null) {
                return true;
            }
        } catch(Exception e) {
        } catch(Error e) {
        }
        return false;
    }
    
    /**
     * @param spi an {@link ILoggerFactorySPI} instance
     */
    public static synchronized void setLoggerFactorySPI(ILoggerFactorySPI spi) {
        if(loggerFactorySPI != null) {
            // avoid redundant ops
            if(loggerFactorySPI.getClass().equals(spi.getClass())) {
                return;
            }
            loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(
                    "Setting another loggerFactorySpi: " + spi.getClass().getName()
                            + " had until now: " + loggerFactorySPI.getClass().getName());
        }
        loggerFactorySPI = spi;
        try {
            throw new RuntimeException("+++ This is not an error. +++");
        } catch(Exception e) {
            String msg = "Logging: Configured XydraLog with " + spi.getClass().getName();
            loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(msg, e);
        }
        loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(
                "New logger factory (" + spi.getClass().getName() + ") is active");
    }
    
    public static synchronized boolean hasLoggerFactorySPI() {
        return loggerFactorySPI != null;
    }
    
    /**
     * Helps debugging.
     * 
     * @return @CanBeNull
     */
    public static ILoggerFactorySPI getLoggerFactorySPI() {
        return loggerFactorySPI;
    }
    
}
