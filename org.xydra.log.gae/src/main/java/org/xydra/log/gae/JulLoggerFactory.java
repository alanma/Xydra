package org.xydra.log.gae;

import java.util.Collection;

import org.xydra.log.ILogListener;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerWithListeners;


public class JulLoggerFactory implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListener) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		if(logListener == null) {
			return new JulLogger(logger);
		} else {
			return new LoggerWithListeners(new JulLogger(logger, logListener), logListener);
		}
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		return new JulLogger(logger, fullyQualifiedNameOfDelegatingLoggerClass);
	}
	
}
