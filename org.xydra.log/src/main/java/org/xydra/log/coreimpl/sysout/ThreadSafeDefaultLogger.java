package org.xydra.log.coreimpl.sysout;

import org.xydra.annotations.ThreadSafe;

/**
 * A thread-safe version of {@link DefaultLogger}.
 *
 * @author kaidel
 */
@ThreadSafe
public class ThreadSafeDefaultLogger extends DefaultLogger {

	private final Object levelLock;

	private final Object loggingLock;

	public ThreadSafeDefaultLogger(final String name) {
		super(name);

		this.levelLock = new Object();
		this.loggingLock = new Object();
	}

	@Override
	public void debug(final String msg) {
		synchronized (this.loggingLock) {
			super.debug(msg);
		}
	}

	@Override
	public void debug(final String msg, final Throwable t) {
		synchronized (this.loggingLock) {
			super.debug(msg, t);
		}
	}

	@Override
	public void error(final String msg) {
		synchronized (this.loggingLock) {
			super.error(msg);
		}
	}

	@Override
	public void error(final String msg, final Throwable t) {
		synchronized (this.loggingLock) {
			super.error(msg, t);
		}
	}

	@Override
	public void info(final String msg) {
		synchronized (this.loggingLock) {
			super.info(msg);
		}
	}

	@Override
	public void info(final String msg, final Throwable t) {
		synchronized (this.loggingLock) {
			super.info(msg, t);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		synchronized (this.levelLock) {
			return super.isDebugEnabled();
		}
	}

	@Override
	public boolean isErrorEnabled() {
		synchronized (this.levelLock) {
			return super.isDebugEnabled();
		}
	}

	@Override
	public boolean isInfoEnabled() {
		synchronized (this.levelLock) {
			return super.isInfoEnabled();
		}
	}

	@Override
	public boolean isTraceEnabled() {
		synchronized (this.levelLock) {
			return super.isTraceEnabled();
		}
	}

	@Override
	public boolean isWarnEnabled() {
		synchronized (this.levelLock) {
			return super.isWarnEnabled();
		}
	}

	@Override
	public void trace(final String msg) {
		synchronized (this.loggingLock) {
			super.trace(msg);
		}
	}

	@Override
	public void trace(final String msg, final Throwable t) {
		synchronized (this.loggingLock) {
			super.trace(msg, t);
		}
	}

	@Override
	public void warn(final String msg) {
		synchronized (this.loggingLock) {
			super.warn(msg);
		}
	}

	@Override
	public void warn(final String msg, final Throwable t) {
		synchronized (this.loggingLock) {
			super.warn(msg, t);
		}
	}
}
