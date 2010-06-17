package org.xydra.log;

public class DefaultLoggerFactorySPI implements ILoggerFactorySPI {
	
	public Logger getLogger(String name) {
		return new DefaultLogger(name);
	}
	
}
