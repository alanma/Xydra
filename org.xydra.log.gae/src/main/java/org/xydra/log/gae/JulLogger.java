package org.xydra.log.gae;

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
	
	public JulLogger(java.util.logging.Logger julLogger) {
		super();
		this.jul = julLogger;
	}
	
	@Override
	public void debug(String msg) {
		this.jul.log(java.util.logging.Level.FINE, msg);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		this.jul.log(java.util.logging.Level.FINE, msg, t);
	}
	
	@Override
	public void error(String msg) {
		this.jul.log(java.util.logging.Level.SEVERE, msg);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.jul.log(java.util.logging.Level.SEVERE, msg, t);
	}
	
	@Override
	public void info(String msg) {
		this.jul.log(java.util.logging.Level.INFO, msg);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.jul.log(java.util.logging.Level.INFO, msg, t);
	}
	
	@Override
	public void trace(String msg) {
		this.jul.log(java.util.logging.Level.FINEST, msg);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.jul.log(java.util.logging.Level.FINEST, msg, t);
	}
	
	@Override
	public void warn(String msg) {
		this.jul.log(java.util.logging.Level.WARNING, msg);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.jul.log(java.util.logging.Level.WARNING, msg, t);
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
}
