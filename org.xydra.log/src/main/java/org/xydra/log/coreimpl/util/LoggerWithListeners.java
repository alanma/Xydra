package org.xydra.log.coreimpl.util;

import java.util.ArrayList;
import java.util.Collection;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;

/**
 * Log to logger and delegate to {@link ILogListener}.
 *
 * @author xamde
 */
@org.xydra.annotations.NotThreadSafe
public class LoggerWithListeners implements Logger {

	private final Logger log;

	@NeverNull
	protected final Collection<ILogListener> logListeners;

	/**
	 * @param logger @NeverNull
	 * @param logListeners
	 * @CanBeNull
	 */
	public LoggerWithListeners(final Logger logger, @CanBeNull final Collection<ILogListener> logListeners) {
		assert logger != null;
		this.log = logger;
		/*
		 * always create object, to be used to synchronise threads in the
		 * subclass
		 */
		this.logListeners = logListeners == null ? new ArrayList<ILogListener>(0) : logListeners;
		assert this.logListeners != null;
	}

	@Override
	public String getName() {
		return this.log.getName();
	}

	@Override
	public void debug(final String msg) {
		this.log.debug(msg);

		for (final ILogListener logListener : this.logListeners) {
			logListener.debug(this.log, msg);
		}
	}

	@Override
	public void debug(final String msg, final Throwable t) {
		this.log.debug(msg, t);

		for (final ILogListener logListener : this.logListeners) {
			logListener.debug(this.log, msg, t);
		}
	}

	@Override
	public void error(final String msg) {
		this.log.error(msg);

		for (final ILogListener logListener : this.logListeners) {
			logListener.error(this.log, msg);
		}
	}

	@Override
	public void error(final String msg, final Throwable t) {
		this.log.error(msg, t);

		for (final ILogListener logListener : this.logListeners) {
			logListener.error(this.log, msg, t);
		}
	}

	@Override
	public void info(final String msg) {
		this.log.info(msg);

		for (final ILogListener logListener : this.logListeners) {
			logListener.info(this.log, msg);
		}
	}

	@Override
	public void info(final String msg, final Throwable t) {
		this.log.info(msg, t);

		for (final ILogListener logListener : this.logListeners) {
			logListener.info(this.log, msg, t);
		}
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
	public void trace(final String msg) {
		this.log.trace(msg);

		for (final ILogListener logListener : this.logListeners) {
			logListener.trace(this.log, msg);
		}
	}

	@Override
	public void trace(final String msg, final Throwable t) {
		this.log.trace(msg, t);

		for (final ILogListener logListener : this.logListeners) {
			logListener.trace(this.log, msg, t);
		}
	}

	@Override
	public void warn(final String msg) {
		this.log.warn(msg);

		for (final ILogListener logListener : this.logListeners) {
			logListener.warn(this.log, msg);
		}
	}

	@Override
	public void warn(final String msg, final Throwable t) {
		this.log.warn(msg, t);

		for (final ILogListener logListener : this.logListeners) {
			logListener.warn(this.log, msg, t);
		}
	}

	@Override
	public void setLevel(final Level level) {
		this.log.setLevel(level);
	}

}
