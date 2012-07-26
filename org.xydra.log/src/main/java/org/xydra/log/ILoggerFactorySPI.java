package org.xydra.log;

import java.util.Collection;


public interface ILoggerFactorySPI {
	
	/**
	 * Return an appropriate {@link DefaultLogger} instance as specified by the
	 * <code>name</code> parameter.
	 * 
	 * <p>
	 * If the name parameter is equal to {@link DefaultLogger#ROOT_LOGGER_NAME},
	 * that is the string value "ROOT" (case insensitive), then the root logger
	 * of the underlying logging system is returned.
	 * 
	 * <p>
	 * Null-valued name arguments are considered invalid.
	 * 
	 * <p>
	 * Certain extremely simple logging systems, e.g. NOP, may always return the
	 * same logger instance regardless of the requested name.
	 * 
	 * @param name the name of the Logger to return
	 * @param logListeners if not null, send all log events also to listeners
	 * @return a logger instance with the given name
	 */
	Logger getLogger(String name, Collection<ILogListener> logListeners);
	
	/**
	 * Return an appropriate thread-safe {@link DefaultLogger} instance as
	 * specified by the <code>name</code> parameter.
	 * 
	 * <p>
	 * If the name parameter is equal to {@link DefaultLogger#ROOT_LOGGER_NAME},
	 * that is the string value "ROOT" (case insensitive), then the root logger
	 * of the underlying logging system is returned.
	 * 
	 * <p>
	 * Null-valued name arguments are considered invalid.
	 * 
	 * <p>
	 * Certain extremely simple logging systems, e.g. NOP, may always return the
	 * same logger instance regardless of the requested name.
	 * 
	 * @param name the name of the Logger to return
	 * @param logListeners if not null, send all log events also to listeners
	 * @return a logger instance with the given name
	 */
	Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners);
	
	/**
	 * OPTIONAL OPERATION
	 * 
	 * @param name
	 * @param fullyQualifiedNameOfDelegatingLoggerClass TODO
	 * @return a logger wrapped in a delegating class (which should not appear
	 *         in stack traces)
	 */
	Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass);
	
	/*
	 * TODO this probably also needs a thread-safe version, right?
	 */
	
}
