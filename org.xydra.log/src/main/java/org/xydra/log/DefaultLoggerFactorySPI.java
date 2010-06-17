package org.xydra.log;

public class DefaultLoggerFactorySPI implements ILoggerFactorySPI {
	
	public DefaultLogger getLogger(String name) {
		return new DefaultLogger(name);
	}
	
}
