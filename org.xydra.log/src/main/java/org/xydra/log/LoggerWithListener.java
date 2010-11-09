package org.xydra.log;

/**
 * Log to logger and delegate to {@link ILogListener}.
 * 
 * @author voelkel
 */
public class LoggerWithListener extends Logger {
	
	private ILogListener logListener;
	private Logger log;
	
	public LoggerWithListener(Logger logger, ILogListener logListener) {
		this.log = logger;
		this.logListener = logListener;
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
		this.logListener.debug(this.log, msg, t);
	}
	
	@Override
	public void debug(String msg) {
		this.log.debug(msg);
		this.logListener.debug(this.log, msg);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		this.log.error(msg, t);
		this.logListener.error(this.log, msg, t);
	}
	
	@Override
	public void error(String msg) {
		this.log.error(msg);
		this.logListener.error(this.log, msg);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		this.log.info(msg, t);
		this.logListener.info(this.log, msg, t);
	}
	
	@Override
	public void info(String msg) {
		this.log.info(msg);
		this.logListener.info(this.log, msg);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		this.log.trace(msg, t);
		this.logListener.trace(this.log, msg, t);
	}
	
	@Override
	public void trace(String msg) {
		this.log.trace(msg);
		this.logListener.trace(this.log, msg);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		this.log.warn(msg, t);
		this.logListener.warn(this.log, msg, t);
	}
	
	@Override
	public void warn(String msg) {
		this.log.warn(msg);
		this.logListener.warn(this.log, msg);
	}
	
}
