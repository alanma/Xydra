package org.xydra.log.gae;

import org.xydra.log.ILogListener;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerWithListener;


public class Log4jLoggerFactory implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name, ILogListener logListener) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
		if(logListener != null) {
			Logger xydraLog4j = new Log4jLogger(LoggerWithListener.class.getName(), logger);
			return new LoggerWithListener(xydraLog4j, logListener);
		} else {
			return new Log4jLogger(Log4jLogger.class.getName(), logger);
		}
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
		return new Log4jLogger(fullyQualifiedNameOfDelegatingLoggerClass, logger);
	}
	
}
