package org.xydra.log.gae;

import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;


public class JulLoggerFactory implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		return new JulLogger(logger);
	}
	
}
