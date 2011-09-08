package org.xydra.log.gae;

import java.util.Collection;
import java.util.logging.LogRecord;

import org.xydra.log.ILogListener;
import org.xydra.log.Logger;


/**
 * Mapping
 * 
 * <pre>
 * FINEST  = trace()
 * FINER   > debug()
 * FINE    = debug()
 * INFO    = info()
 * CONFIG  > info()
 * WARNING = warn()
 * SEVERE  = error()
 * </pre>
 * 
 * @author voelkel
 */
public class JulLogger extends Logger {
	
	private java.util.logging.Logger jul;
	private Collection<ILogListener> logListeners;
	
	public JulLogger(java.util.logging.Logger julLogger) {
		super();
		this.jul = julLogger;
	}
	
	public JulLogger(java.util.logging.Logger julLogger, Collection<ILogListener> logListeners) {
		super();
		this.jul = julLogger;
		this.logListeners = logListeners;
	}
	
	public JulLogger(java.util.logging.Logger logger,
	        String fullyQualifiedNameOfDelegatingLoggerClass) {
		this(logger);
		// TODO fix and make sure stacktrace is compute correctly
	}
	
	private LogRecord createLogRecord(java.util.logging.Level level, String msg) {
		LogRecord record = new LogRecord(level, msg);
		setCorrectCallerClassAndMethod(record);
		return record;
	}
	
	private LogRecord createLogRecord(java.util.logging.Level level, String msg, Throwable t) {
		LogRecord record = new LogRecord(level, msg);
		record.setThrown(t);
		setCorrectCallerClassAndMethod(record);
		return record;
	}
	
	private void setCorrectCallerClassAndMethod(LogRecord record) {
		try {
			throw new RuntimeException("trigger");
		} catch(RuntimeException e) {
			e.fillInStackTrace();
			e.getStackTrace();
			if(this.logListeners == null) {
				record.setSourceClassName(e.getStackTrace()[3].getClassName());
				record.setSourceMethodName(e.getStackTrace()[3].getMethodName());
				record.setMessage(record.getMessage());
				// e.getStackTrace()[1].getLineNumber();
			} else {
				record.setSourceClassName(e.getStackTrace()[4].getClassName());
				record.setSourceMethodName(e.getStackTrace()[4].getMethodName());
				// e.getStackTrace()[1].getLineNumber();
				record.setMessage(record.getMessage());
			}
		}
		
	}
	
	@Override
	public void debug(String msg) {
		this.jul.log(createLogRecord(java.util.logging.Level.FINE, msg));
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		this.jul.log(createLogRecord(java.util.logging.Level.FINE, msg, t));
	}
	
	@Override
	public void error(String msg) {
		this.jul.log(createLogRecord(java.util.logging.Level.SEVERE, msg));
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.jul.log(createLogRecord(java.util.logging.Level.SEVERE, msg, t));
	}
	
	@Override
	public void info(String msg) {
		this.jul.log(createLogRecord(java.util.logging.Level.INFO, msg));
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.jul.log(createLogRecord(java.util.logging.Level.INFO, msg, t));
	}
	
	@Override
	public void trace(String msg) {
		this.jul.log(createLogRecord(java.util.logging.Level.FINEST, msg));
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.jul.log(createLogRecord(java.util.logging.Level.FINEST, msg, t));
	}
	
	@Override
	public void warn(String msg) {
		this.jul.log(createLogRecord(java.util.logging.Level.WARNING, msg));
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.jul.log(createLogRecord(java.util.logging.Level.WARNING, msg, t));
	}
	
	@Override
	public boolean isDebugEnabled() {
		return this.jul.isLoggable(java.util.logging.Level.FINE);
	}
	
	@Override
	public boolean isErrorEnabled() {
		return this.jul.isLoggable(java.util.logging.Level.SEVERE);
	}
	
	@Override
	public boolean isInfoEnabled() {
		return this.jul.isLoggable(java.util.logging.Level.INFO);
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.jul.isLoggable(java.util.logging.Level.FINEST);
	}
	
	@Override
	public boolean isWarnEnabled() {
		return this.jul.isLoggable(java.util.logging.Level.WARNING);
	}
	
	@Override
	public String toString() {
		return this.jul.getName();
	}
	
}
