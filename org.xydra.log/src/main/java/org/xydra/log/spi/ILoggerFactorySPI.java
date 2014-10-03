package org.xydra.log.spi;

import java.util.Collection;

import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;

/**
 * The Service Provider Interface (SPI) for logger factory implementations
 * 
 * @author xamde
 */
public interface ILoggerFactorySPI {

	/**
	 * Return an appropriate {@link Logger} instance as specified by the
	 * <code>name</code> parameter.
	 * 
	 * <p>
	 * If the name parameter is equal to {@link Logger#ROOT_LOGGER_NAME}, that
	 * is the string value "ROOT" (case insensitive), then the root logger of
	 * the underlying logging system is returned.
	 * 
	 * <p>
	 * Certain extremely simple logging systems, e.g. NOP, may always return the
	 * same logger instance regardless of the requested name.
	 * 
	 * @param name
	 *            the name of the {@link Logger} to return @NeverNull
	 * @param logListeners
	 *            @CanBeNull if not null, send all log events also to listeners
	 * @return a logger instance with the given name
	 */
	Logger getLogger(String name, Collection<ILogListener> logListeners);

	/**
	 * Return an appropriate thread-safe {@link Logger} instance as specified by
	 * the <code>name</code> parameter.
	 * 
	 * Thread-safe loggers might come at an extra cost, so this provider offers
	 * the option to return potentially unsafe loggers for performance reasons.
	 * 
	 * <p>
	 * If the name parameter is equal to {@link Logger#ROOT_LOGGER_NAME}, that
	 * is the string value "ROOT" (case insensitive), then the root logger of
	 * the underlying logging system is returned.
	 * 
	 * <p>
	 * Certain extremely simple logging systems, e.g. NOP, may always return the
	 * same logger instance regardless of the requested name.
	 * 
	 * @param name
	 *            the name of the {@link Logger} to return @NeverNull
	 * @param logListeners
	 *            @CanBeNull if not null, send all log events also to listeners
	 * @return a logger instance with the given name
	 */
	Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners);

	/**
	 * OPTIONAL OPERATION
	 * 
	 * Thread-safe loggers might come at an extra cost, so this provider offers
	 * the option to return potentially unsafe loggers for performance reasons.
	 * 
	 * @param name
	 *            the name of the {@link Logger} to return @NeverNull
	 * @param fullyQualifiedNameOfDelegatingLoggerClass
	 *            this class is filtered out from stack-traces
	 * @return a logger wrapped in a delegating class (which should not appear
	 *         in stack traces)
	 */
	Logger getThreadSafeWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass);

	/**
	 * OPTIONAL OPERATION
	 * 
	 * @param name
	 *            the name of the {@link Logger} to return @NeverNull
	 * @param fullyQualifiedNameOfDelegatingLoggerClass
	 *            this class is filtered out from stack-traces
	 * @return a logger wrapped in a delegating class (which should not appear
	 *         in stack traces)
	 */
	Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass);

}
