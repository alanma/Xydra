package org.xydra.log;

public class LoggerFactory {
	
	private static ILoggerFactorySPI loggerFactorySPI;
	
	public static Logger getLogger(Class<?> clazz) {
		if(loggerFactorySPI == null) {
			loggerFactorySPI = new DefaultLoggerFactorySPI();
			loggerFactorySPI.getLogger("ROOT").error(
			        "Found no LoggerFactorySPI, using default to std.out");
		}
		return loggerFactorySPI.getLogger(clazz.getName());
	}
	
}
