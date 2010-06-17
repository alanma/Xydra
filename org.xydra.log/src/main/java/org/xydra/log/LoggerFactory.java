package org.xydra.log;

import com.google.gwt.core.client.GWT;


public class LoggerFactory {
	
	private static ILoggerFactorySPI loggerFactorySPI;
	
	public static Logger getLogger(Class<?> clazz) {
		if(loggerFactorySPI == null) {
			
			// try to use GWT logger
			
			if(gwtEnabled()) {
				// FIXME
				System.out.println("Hey, we run GWT!");
				GWT.log("I can see you", null);
			}
			
			loggerFactorySPI = new DefaultLoggerFactorySPI();
			loggerFactorySPI.getLogger("ROOT").error(
			        "Found no LoggerFactorySPI, using default to std.out");
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
	
}
