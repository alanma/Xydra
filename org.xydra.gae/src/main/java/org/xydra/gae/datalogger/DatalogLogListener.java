package org.xydra.gae.datalogger;

import org.xydra.log.ILogListener;
import org.xydra.log.Logger;


/**
 * Listens to log messages of all levels and forwards to {@link DataLogger}.
 * 
 * @author xamde
 */
public class DatalogLogListener implements ILogListener {
	
	public static final int TRACE = 0;
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		DataLogger.handleLog(log, WARN, msg, t);
	}
	
	@Override
	public void warn(Logger log, String msg) {
		DataLogger.handleLog(log, WARN, msg, null);
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
		DataLogger.handleLog(log, TRACE, msg, t);
	}
	
	@Override
	public void trace(Logger log, String msg) {
		DataLogger.handleLog(log, TRACE, msg, null);
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
		DataLogger.handleLog(log, INFO, msg, t);
	}
	
	@Override
	public void info(Logger log, String msg) {
		DataLogger.handleLog(log, INFO, msg, null);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		DataLogger.handleLog(log, ERROR, msg, t);
	}
	
	@Override
	public void error(Logger log, String msg) {
		DataLogger.handleLog(log, ERROR, msg, null);
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
		DataLogger.handleLog(log, DEBUG, msg, t);
	}
	
	@Override
	public void debug(Logger log, String msg) {
		DataLogger.handleLog(log, DEBUG, msg, null);
	}
	
}
