package org.xydra.log.gae;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.jul.JulLoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.log.impl.universal.UniversalLogger;
import org.xydra.log.spi.ILoggerFactorySPI;

import com.google.appengine.api.utils.SystemProperty;

/**
 * In development mode log4j is used. In production, j.u.l. is used.
 *
 * @author xamde
 * @deprecated use {@link UniversalLogger}
 */
@ThreadSafe
@Deprecated
public class GaeLoggerFactorySPI implements ILoggerFactorySPI {

	private static ILoggerFactorySPI factory = null;

	/**
	 * Create and register appropriate factory.
	 */
	public GaeLoggerFactorySPI() {
		if (factory == null) {
			if (inProduction()) {
				factory = new JulLoggerFactory();
			} else {
				factory = new Log4jLoggerFactory();
			}
			LoggerFactory.setLoggerFactorySPI(factory, "GaeLoggerFactorySPI()");
		}
	}

	@Override
	public Logger getLogger(final String name, final Collection<ILogListener> logListener) {
		return factory.getLogger(name, logListener);
	}

	@Override
	public Logger getWrappedLogger(final String name, final String fullyQualifiedNameOfDelegatingLoggerClass) {
		return factory.getWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
	}

	/**
	 * @return true if app is running on a real remote GAE server
	 */
	public static boolean inProduction() {
		return SystemProperty.environment.get() != null
				&& SystemProperty.environment.value().equals(
						SystemProperty.Environment.Value.Production);
	}

	@SuppressWarnings("unused")
	public static void init() {
		if (factory == null) {
			new GaeLoggerFactorySPI();
		}
	}

	@Override
	public Logger getThreadSafeLogger(final String name, final Collection<ILogListener> logListeners) {
		return factory.getThreadSafeLogger(name, logListeners);
	}

	@Override
	public Logger getThreadSafeWrappedLogger(final String name,
			final String fullyQualifiedNameOfDelegatingLoggerClass) {
		return factory.getThreadSafeWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
	}

}
