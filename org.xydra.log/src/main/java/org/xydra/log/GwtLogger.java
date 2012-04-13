package org.xydra.log;

/**
 * Delegate to an GWT 2.1 logger
 * 
 * @author xamde
 */
public class GwtLogger extends org.xydra.log.Logger {
	
	private java.util.logging.Logger logger;
	
	public GwtLogger(String name) {
		this.logger = java.util.logging.Logger.getLogger(name);
	}
	
	public GwtLogger(java.util.logging.Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void debug(String msg) {
		this.logger.log(java.util.logging.Level.FINE, msg);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		this.logger.log(java.util.logging.Level.FINE, msg, t);
	}
	
	@Override
	public void error(String msg) {
		this.logger.log(java.util.logging.Level.SEVERE, msg);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.logger.log(java.util.logging.Level.SEVERE, msg, t);
	}
	
	@Override
	public void info(String msg) {
		this.logger.log(java.util.logging.Level.INFO, msg);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.logger.log(java.util.logging.Level.INFO, msg, t);
	}
	
	@Override
	public boolean isDebugEnabled() {
		return this.logger.isLoggable(java.util.logging.Level.FINE);
	}
	
	@Override
	public boolean isErrorEnabled() {
		return this.logger.isLoggable(java.util.logging.Level.SEVERE);
	}
	
	@Override
	public boolean isInfoEnabled() {
		return this.logger.isLoggable(java.util.logging.Level.INFO);
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.logger.isLoggable(java.util.logging.Level.FINEST);
	}
	
	@Override
	public boolean isWarnEnabled() {
		return this.logger.isLoggable(java.util.logging.Level.WARNING);
	}
	
	@Override
	public void trace(String msg) {
		this.logger.log(java.util.logging.Level.FINEST, msg);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.logger.log(java.util.logging.Level.FINEST, msg, t);
	}
	
	@Override
	public void warn(String msg) {
		this.logger.log(java.util.logging.Level.WARNING, msg);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.logger.log(java.util.logging.Level.WARNING, msg, t);
	}
	
	@Override
	public String toString() {
		return this.logger.getName();
	}
	
}
