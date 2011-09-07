package org.xydra.log;

public class GwtLoggerFactorySPI implements ILoggerFactorySPI {
	
	@Override
    public Logger getLogger(String name, ILogListener logListener) {
		return new GwtLogger(name);
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}
}
