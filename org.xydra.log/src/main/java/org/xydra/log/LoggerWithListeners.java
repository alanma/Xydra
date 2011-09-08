package org.xydra.log;

import java.util.Collection;


/**
 * Log to logger and delegate to {@link ILogListener}.
 * 
 * @author voelkel
 */
public class LoggerWithListeners extends Logger {
	
	private Collection<ILogListener> logListeners;
	private Logger log;
	
	public LoggerWithListeners(Logger logger, Collection<ILogListener> logListeners) {
		this.log = logger;
		this.logListeners = logListeners;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return this.log.isDebugEnabled();
	}
	
	@Override
	public boolean isErrorEnabled() {
		return this.log.isErrorEnabled();
	}
	
	@Override
	public boolean isInfoEnabled() {
		return this.log.isInfoEnabled();
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.log.isTraceEnabled();
	}
	
	@Override
	public boolean isWarnEnabled() {
		return this.log.isWarnEnabled();
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		this.log.debug(msg, t);
		for(ILogListener logListener : this.logListeners) {
			logListener.debug(this.log, msg, t);
		}
	}
	
	@Override
	public void debug(String msg) {
		this.log.debug(msg);
		for(ILogListener logListener : this.logListeners) {
			logListener.debug(this.log, msg);
		}
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.log.error(msg, t);
		for(ILogListener logListener : this.logListeners) {
			logListener.error(this.log, msg, t);
		}
	}
	
	@Override
	public void error(String msg) {
		this.log.error(msg);
		for(ILogListener logListener : this.logListeners) {
			logListener.error(this.log, msg);
		}
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.log.info(msg, t);
		for(ILogListener logListener : this.logListeners) {
			logListener.info(this.log, msg, t);
		}
	}
	
	@Override
	public void info(String msg) {
		this.log.info(msg);
		for(ILogListener logListener : this.logListeners) {
			logListener.info(this.log, msg);
		}
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.log.trace(msg, t);
		for(ILogListener logListener : this.logListeners) {
			logListener.trace(this.log, msg, t);
		}
	}
	
	@Override
	public void trace(String msg) {
		this.log.trace(msg);
		for(ILogListener logListener : this.logListeners) {
			logListener.trace(this.log, msg);
		}
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.log.warn(msg, t);
		for(ILogListener logListener : this.logListeners) {
			logListener.warn(this.log, msg, t);
		}
	}
	
	@Override
	public void warn(String msg) {
		this.log.warn(msg);
		for(ILogListener logListener : this.logListeners) {
			logListener.warn(this.log, msg);
		}
	}
	
}
