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
	public Logger getLogger(final String name, final Collection<ILogListener> logListeners) {
		return new DefaultLogger(name, logListeners);
	}

	@Override
	public Logger getThreadSafeLogger(final String name, final Collection<ILogListener> logListeners) {
		final Logger logger = new ThreadSafeDefaultLogger(name);
		return new ThreadSafeLoggerWithListeners(logger, logListeners);
	}

	@Override
	public Logger getThreadSafeWrappedLogger(final String name,
			final String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Logger getWrappedLogger(final String name, final String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}

}
