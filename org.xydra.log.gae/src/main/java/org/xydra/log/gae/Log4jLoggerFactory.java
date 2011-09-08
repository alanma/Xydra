package org.xydra.log.gae;

import java.util.Collection;

import org.xydra.log.ILogListener;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerWithListeners;


public class Log4jLoggerFactory implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListeners) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
		if(logListeners != null) {
			Logger xydraLog4j = new Log4jLogger(LoggerWithListeners.class.getName(), logger);
			return new LoggerWithListeners(xydraLog4j, logListeners);
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
