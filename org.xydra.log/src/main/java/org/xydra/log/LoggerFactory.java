package org.xydra.log;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;


public class LoggerFactory {
	
	private static ILoggerFactorySPI loggerFactorySPI;
	
	public static Logger getLogger(Class<?> clazz) {
		if(loggerFactorySPI == null) {
			
			// try to use GWT logger
			
			if(gwtEnabled() && gwtLogEnabled()) {
				
				loggerFactorySPI = new GwtLoggerFactorySPI();
				// FIXME
				System.out.println("Hey, we run GWT!");
				GWT.log("I can see you", null);
				
			} else {
				
				loggerFactorySPI = new DefaultLoggerFactorySPI();
				loggerFactorySPI.getLogger("ROOT").error(
				        "Found no LoggerFactorySPI, using default to std.out");
				
			}
			
		}
		return loggerFactorySPI.getLogger(clazz.getName());
	}
	
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
	
}
