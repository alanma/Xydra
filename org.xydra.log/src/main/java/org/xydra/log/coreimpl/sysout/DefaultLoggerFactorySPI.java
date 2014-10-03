package org.xydra.log.coreimpl.sysout;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.coreimpl.util.ThreadSafeLoggerWithListeners;
import org.xydra.log.spi.ILoggerFactorySPI;

@ThreadSafe
public class DefaultLoggerFactorySPI implements ILoggerFactorySPI {

	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListeners) {
		return new DefaultLogger(name, logListeners);
	}

	@Override
	public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
		Logger logger = new ThreadSafeDefaultLogger(name);
		return new ThreadSafeLoggerWithListeners(logger, logListeners);
	}

	@Override
	public Logger getThreadSafeWrappedLogger(String name,
			String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}

}
