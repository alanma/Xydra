package org.xydra.log.impl.jul;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.coreimpl.util.LoggerWithListeners;
import org.xydra.log.coreimpl.util.ThreadSafeLoggerWithListeners;
import org.xydra.log.spi.ILoggerFactorySPI;

/**
 * A {@link ILoggerFactorySPI} using Java.Utils.Logging (JUL) internally.
 *
 * @author xamde
 */
@ThreadSafe
public class JulLoggerFactory implements ILoggerFactorySPI {

	@Override
	public Logger getLogger(final String name, final Collection<ILogListener> logListeners) {
		final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		if (logListeners == null || logListeners.isEmpty()) {
			return new JulLogger(logger);
		} else {
			return new LoggerWithListeners(JulLogger.createWithListeners(logger), logListeners);
		}
	}

	@Override
	public Logger getWrappedLogger(final String name, final String fullyQualifiedNameOfDelegatingLoggerClass) {
		final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		return new JulLogger(logger, fullyQualifiedNameOfDelegatingLoggerClass);
	}

	@Override
	public Logger getThreadSafeLogger(final String name, final Collection<ILogListener> logListeners) {
		final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		if (logListeners == null || logListeners.isEmpty()) {
			return new JulLogger(logger);
		} else {
			return new ThreadSafeLoggerWithListeners(JulLogger.createWithListeners(logger),
					logListeners);
		}
	}

	@Override
	public Logger getThreadSafeWrappedLogger(final String name,
			final String fullyQualifiedNameOfDelegatingLoggerClass) {
		// getWrappedLogger already returns a thread-safe logger
		return getWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
	}

}
