package org.xydra.log;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;


/**
 * The basic logger factory. Same signature to obtain a {@link Logger} as slf4j.
 * 
 * @author voelkel
 * 
 */
public class LoggerFactory {
	
	private static ILoggerFactorySPI loggerFactorySPI;
	private static Set<ILogListener> logListeners_ = new HashSet<ILogListener>();
	
	public static final String ROOT_LOGGER_NAME = "org.xydra.log.system";
	
	/**
	 * All log messages are also sent to a registered {@link ILogListener}.
	 * 
	 * @param logListener a listener to receive all log messages
	 */
	public static void addLogListener(ILogListener logListener) {
		logListeners_.add(logListener);
		loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(
		        "Logging: Attached log listener " + logListener.getClass().getName());
	}
	
	public static void removeLogListener(ILogListener logListener) {
		logListeners_.remove(logListener);
		loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(
		        "Logging: Removed log listener " + logListener.getClass().getName());
	}
	
	/**
	 * @param clazz used as a name for the logger
	 * @return a logger, using the sub-system configured by
	 */
	public static Logger getLogger(Class<?> clazz) {
		if(loggerFactorySPI == null) {
			init();
		}
		return loggerFactorySPI.getLogger(clazz.getName(), logListeners_);
	}
	
	private static void init() {
		// try to use GWT logger
		if(gwtEnabled() && gwtLogEnabled()) {
			loggerFactorySPI = new GwtLoggerFactorySPI();
			loggerFactorySPI.getLogger("ROOT", null).info(
			        "Logging: Found no LoggerFactorySPI, using GWT Log");
		} else {
			loggerFactorySPI = new DefaultLoggerFactorySPI();
			loggerFactorySPI.getLogger("ROOT", null).error(
			        "Logging: Found no LoggerFactorySPI, using default to std.out");
			try {
				throw new RuntimeException(
				        "Logging: FYI (Not an error) This was the first logging call which triggered the log init(). ");
			} catch(RuntimeException e) {
				loggerFactorySPI.getLogger("ROOT", null).info(
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
	public static void setLoggerFactorySPI(ILoggerFactorySPI spi) {
		if(loggerFactorySPI == null || !loggerFactorySPI.getClass().equals(spi.getClass())) {
			loggerFactorySPI = spi;
			try {
				throw new RuntimeException("+++ This is not an error. +++");
			} catch(Exception e) {
				String msg = "Logging: Configured XydraLog with " + spi.getClass().getName();
				loggerFactorySPI.getLogger(ROOT_LOGGER_NAME, null).info(msg, e);
			}
		}
	}
	
	public static boolean hasLoggerFactorySPI() {
		return loggerFactorySPI != null;
	}
	
}
