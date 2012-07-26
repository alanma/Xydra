package org.xydra.log;

import java.util.Collection;


/**
 * Log to logger and delegate to {@link ILogListener}. Thread-safe version of
 * {@link LoggerWithListeners}.
 * 
 * @author Kaidel
 */
public class ThreadSafeLoggerWithListeners extends LoggerWithListeners implements IThreadSafe {
	
	private Object loggingLock;
	
	/**
	 * Constructs a new instance.
	 * 
	 * The given logger does not necessarily need to be thread-safe, as long as
	 * it is not shared between multiple threads and only used in the instance
	 * created by this constructor.
	 * 
	 * TODO is this correct?
	 * 
	 * @throws RuntimeException if the given logger is not thread-safe (i.e.
	 *             does not implement the {@link IThreadSafe} interface)
	 */
	public ThreadSafeLoggerWithListeners(Logger logger, Collection<ILogListener> logListeners) {
		super(logger, logListeners);
		
		this.loggingLock = new Object();
		
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		synchronized(this.loggingLock) {
			super.debug(msg, t);
		}
	}
	
	@Override
	public void debug(String msg) {
		synchronized(this.loggingLock) {
			super.debug(msg);
		}
	}
	
	@Override
	public void error(String msg, Throwable t) {
		synchronized(this.loggingLock) {
			super.error(msg, t);
		}
	}
	
	@Override
	public void error(String msg) {
		synchronized(this.loggingLock) {
			super.error(msg);
		}
	}
	
	@Override
	public void info(String msg, Throwable t) {
		synchronized(this.loggingLock) {
			super.info(msg, t);
		}
	}
	
	@Override
	public void info(String msg) {
		synchronized(this.loggingLock) {
			super.info(msg);
		}
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		synchronized(this.loggingLock) {
			super.trace(msg, t);
		}
	}
	
	@Override
	public void trace(String msg) {
		synchronized(this.loggingLock) {
			super.trace(msg);
		}
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		synchronized(this.loggingLock) {
			super.warn(msg, t);
		}
	}
	
	@Override
	public void warn(String msg) {
		synchronized(this.loggingLock) {
			super.warn(msg);
		}
	}
	
}
