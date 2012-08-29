package org.xydra.log;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;


/**
 * Delegate to an GWT 2.1 logger
 * 
 * @author xamde
 */
@ThreadSafe
public class GwtLoggerFactorySPI implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListeners) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		Logger gwtLogger = new GwtLogger(logger);
		if(logListeners != null) {
			return new LoggerWithListeners(gwtLogger, logListeners);
		} else {
			return gwtLogger;
		}
	}
	
	@Override
	public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		Logger gwtLogger = new GwtLogger(logger);
		if(logListeners != null) {
			return new ThreadSafeLoggerWithListeners(gwtLogger, logListeners);
		} else {
			return gwtLogger;
		}
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}
}
