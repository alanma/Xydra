package org.xydra.log.gae;

import java.util.logging.LogManager;

import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;


public class JulLoggerFactory implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name) {
		java.util.logging.Logger logger = LogManager.getLogManager().getLogger(name);
		return new JulLogger(logger);
	}
	
}
