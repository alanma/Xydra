package org.xydra.log;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;


/**
 * Log to logger and delegate to {@link ILogListener}. Thread-safe version of
 * {@link LoggerWithListeners}.
 * 
 * @author Kaidel
 */
@ThreadSafe
public class ThreadSafeLoggerWithListeners extends LoggerWithListeners {
	
	/**
	 * Constructs a new instance.
	 * 
	 * The given logger needs to be thread-safe. TODO is this the way to go?
	 * Maybe there's an option to check whether the given logger is annotated
	 * with "@ThreadSafe"
	 * 
	 * @param logger
	 * @param logListeners
	 */
	public ThreadSafeLoggerWithListeners(Logger logger, Collection<ILogListener> logListeners) {
		super(logger, logListeners);
	}
	
	/*
	 * We're explicitly synchronizing on the logListeners instead of using
	 * synchronized methods or a simple lock object, to ensure that it's
	 * possible to synchronize access on the logListeners when they're shared
	 * between different objects (these objects also need to synchronize their
	 * access to the logListners on the listeners)
	 */
	
	@Override
	public void debug(String msg, Throwable t) {
		synchronized(this.logListeners) {
			super.debug(msg, t);
		}
	}
	
	@Override
	public void debug(String msg) {
		synchronized(this.logListeners) {
			super.debug(msg);
		}
	}
	
	@Override
	public void error(String msg, Throwable t) {
		synchronized(this.logListeners) {
			super.error(msg, t);
		}
	}
	
	@Override
	public void error(String msg) {
		synchronized(this.logListeners) {
			super.error(msg);
		}
	}
	
	@Override
	public void info(String msg, Throwable t) {
		synchronized(this.logListeners) {
			super.info(msg, t);
		}
	}
	
	@Override
	public void info(String msg) {
		synchronized(this.logListeners) {
			super.info(msg);
		}
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		synchronized(this.logListeners) {
			super.trace(msg, t);
		}
	}
	
	@Override
	public void trace(String msg) {
		synchronized(this.logListeners) {
			super.trace(msg);
		}
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		synchronized(this.logListeners) {
			super.warn(msg, t);
		}
	}
	
	@Override
	public void warn(String msg) {
		synchronized(this.logListeners) {
			super.warn(msg);
		}
	}
	
}
