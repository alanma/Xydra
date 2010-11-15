package org.xydra.log;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;


/**
 * The basic logger factory. Same signature to obtain a {@link Logger} as slf4j.
 * 
 * @author voelkel
 * 
 */
public class LoggerFactory {
	
	private static ILoggerFactorySPI loggerFactorySPI;
	private static ILogListener logListener_;
	
	/**
	 * All log messages are also sent to a registered {@link ILogListener}
	 * 
	 * @param logger
	 */
	public static void setLogListener(ILogListener logListener) {
		logListener_ = logListener;
	}
	
	/**
	 * @param clazz
	 * @return a logger, using the sub-system configured by
	 */
	public static Logger getLogger(Class<?> clazz) {
		if(loggerFactorySPI == null) {
			init();
		}
		return loggerFactorySPI.getLogger(clazz.getName(), logListener_);
	}
	
	private static void init() {
		// try to use GWT logger
		if(gwtEnabled() && gwtLogEnabled()) {
			loggerFactorySPI = new GwtLoggerFactorySPI();
			loggerFactorySPI.getLogger("ROOT", null).info(
			        "Found no LoggerFactorySPI, using GWT Log");
		} else {
			loggerFactorySPI = new DefaultLoggerFactorySPI();
			loggerFactorySPI.getLogger("ROOT", null).error(
			        "Found no LoggerFactorySPI, using default to std.out");
			try {
				throw new RuntimeException("this stacktrace caused the log init()");
			} catch(RuntimeException e) {
				loggerFactorySPI.getLogger("ROOT", null).info("Printing caller to System.out", e);
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
			if(Log.LOG_LEVEL_INFO != 0) {
				return true;
			}
		} catch(Exception e) {
		} catch(Error e) {
		}
		return false;
	}
	
	/**
	 * @param spi
	 */
	public static void setLoggerFactorySPI(ILoggerFactorySPI spi) {
		loggerFactorySPI = spi;
		loggerFactorySPI.getLogger("ROOT", null).info(
		        "Configured XydraLog with " + spi.getClass().getName());
	}
	
}
