package org.xydra.log.gae;

import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;


public class Log4jLoggerFactory implements ILoggerFactorySPI {
	
	@Override
	public Logger getLogger(String name) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
		return new Log4jLogger(logger);
	}
	
}
