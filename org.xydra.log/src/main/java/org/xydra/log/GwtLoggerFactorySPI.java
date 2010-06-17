package org.xydra.log;

public class GwtLoggerFactorySPI implements ILoggerFactorySPI {
	
	public Logger getLogger(String name) {
		return new GwtLogger(name);
	}
	
}
