package org.xydra.log;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;


@ThreadSafe
public class DefaultLoggerFactorySPI implements ILoggerFactorySPI {
	
	/*
	 * TODO is there a reason why the methods don't care about the
	 * listeners-parameter?
	 */
	
	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListener) {
		return new DefaultLogger(name);
	}
	
	@Override
	public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListener) {
		return new ThreadSafeDefaultLogger(name);
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}
	
}
