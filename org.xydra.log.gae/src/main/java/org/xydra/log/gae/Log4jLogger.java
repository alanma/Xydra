package org.xydra.log.gae;

import org.xydra.annotations.ThreadSafe;
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
@ThreadSafe
public class Log4jLogger extends Logger {
	
	/*
	 * log4j is already thread-safe (according to the official website:
	 * http://logging.apache.org/log4j/1.2/faq.html#a1.7), so no manual
	 * synchronization needs to be done in this class.
	 */
	private org.apache.log4j.Logger log4j;
	private String callerClassToHideFromStacktrace;
	
	public Log4jLogger(String callerClassToHideFromStacktrace, org.apache.log4j.Logger log4jLogger) {
		super();
		this.callerClassToHideFromStacktrace = callerClassToHideFromStacktrace;
		this.log4j = log4jLogger;
	}
	
	@Override
	public void debug(String msg) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.DEBUG, msg,
		        null);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.DEBUG, msg, t);
	}
	
	@Override
	public void error(String msg) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.ERROR, msg,
		        null);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.ERROR, msg, t);
	}
	
	@Override
	public void info(String msg) {
		this.log4j
		        .log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.INFO, msg, null);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.INFO, msg, t);
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
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.TRACE, msg,
		        null);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.TRACE, msg, t);
	}
	
	@Override
	public void warn(String msg) {
		this.log4j
		        .log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.WARN, msg, null);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.log4j.log(this.callerClassToHideFromStacktrace, org.apache.log4j.Level.WARN, msg, t);
	}
	
	@Override
	public String toString() {
		return this.log4j.getName();
	}
	
}
