package org.xydra.log;

public class DefaultLoggerFactorySPI implements ILoggerFactorySPI {
	
	@Override
    public Logger getLogger(String name, ILogListener logListener) {
		return new DefaultLogger(name);
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}
	
}
