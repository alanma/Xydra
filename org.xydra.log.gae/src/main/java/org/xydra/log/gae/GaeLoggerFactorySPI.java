package org.xydra.log.gae;

import org.xydra.log.ILogListener;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.appengine.api.utils.SystemProperty;


/**
 * In development mode log4j is used. In production, j.u.l. is used.
 * 
 * @author voelkel
 * 
 */
public class GaeLoggerFactorySPI implements ILoggerFactorySPI {
	
	private static ILoggerFactorySPI factory;
	
	static {
		init();
	}
	
	/**
	 * Create and register appropriate factory.
	 */
	public static void init() {
		if(inProduction()) {
			factory = new JulLoggerFactory();
		} else {
			factory = new Log4jLoggerFactory();
		}
		LoggerFactory.setLoggerFactorySPI(factory);
	}
	
	@Override
	public Logger getLogger(String name, ILogListener logListener) {
		return factory.getLogger(name, logListener);
	}
	
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		return factory.getWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
	}
	
	/**
	 * @return true if app is running on a real remote GAE server
	 */
	public static boolean inProduction() {
		return SystemProperty.environment.get() != null
		        && SystemProperty.environment.value().equals(
		                SystemProperty.Environment.Value.Production);
	}
	
}
