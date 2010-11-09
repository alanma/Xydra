package org.xydra.log;

/**
 * Listener for log events
 * 
 * @author voelkel
 */
public interface ILogListener {
	
	void trace(Logger log, String msg);
	
	void trace(Logger log, String msg, Throwable t);
	
	void debug(Logger log, String msg);
	
	void debug(Logger log, String msg, Throwable t);
	
	void info(Logger log, String msg);
	
	void info(Logger log, String msg, Throwable t);
	
	void warn(Logger log, String msg);
	
	void warn(Logger log, String msg, Throwable t);
	
	void error(Logger log, String msg);
	
	void error(Logger log, String msg, Throwable t);
}
