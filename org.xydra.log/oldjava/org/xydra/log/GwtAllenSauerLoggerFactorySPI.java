package org.xydra.log;

import java.util.Collection;


public class GwtAllenSauerLoggerFactorySPI implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListener) {
		return new GwtLogger(name);
	}
	
	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		throw new UnsupportedOperationException();
	}
}
