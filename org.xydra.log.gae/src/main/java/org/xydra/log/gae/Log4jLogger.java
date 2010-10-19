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
public class Log4jLogger extends Logger {
	
	private org.apache.log4j.Logger log4j;
	
	public Log4jLogger(org.apache.log4j.Logger log4jLogger) {
		super();
		this.log4j = log4jLogger;
	}
	
	@Override
	public void debug(String msg) {
		this.log4j.debug(msg);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		this.log4j.debug(msg, t);
	}
	
	@Override
	public void error(String msg) {
		this.log4j.error(msg);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.log4j.error(msg, t);
	}
	
	@Override
	public void info(String msg) {
		this.log4j.info(msg);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.log4j.info(msg, t);
	}
	
	@Override
	public boolean isDebugEnabled() {
		return this.log4j.isDebugEnabled();
	}
	
	@Override
	public boolean isErrorEnabled() {
		return this.log4j.isEnabledFor(org.apache.log4j.Level.ERROR);
	}
	
	@Override
	public boolean isInfoEnabled() {
		return this.log4j.isInfoEnabled();
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.log4j.isTraceEnabled();
	}
	
	@Override
	public boolean isWarnEnabled() {
		return this.log4j.isEnabledFor(org.apache.log4j.Level.WARN);
	}
	
	@Override
	public void trace(String msg) {
		this.log4j.trace(msg);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.log4j.trace(msg, t);
	}
	
	@Override
	public void warn(String msg) {
		this.log4j.warn(msg);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.log4j.warn(msg, t);
	}
	
}
