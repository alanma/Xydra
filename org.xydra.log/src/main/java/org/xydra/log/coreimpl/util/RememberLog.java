package org.xydra.log.coreimpl.util;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.log.api.Logger;
import org.xydra.log.util.SharedExceptionUtils;

/**
 * Remembers all log input up to a certain memory limit. Keeps always the piece
 * of the earliest and another piece of the latest logs, even if space becomes
 * exhausted.
 *
 * @author xamde
 */
public class RememberLog implements Logger {

	private static final String LINEEND = "\n";

	private final LimitedBuffer buf = new LimitedBuffer(100 * 1024, LINEEND);

	private Level minLevel;

	@CanBeNull
	private final Logger delegate;

	/**
	 * @param minLevel
	 *            all logs below this level are discarded
	 */
	public RememberLog(final Level minLevel) {
		this(minLevel, null);
	}

	public RememberLog(final Level minLevel, final Logger delegate) {
		this.minLevel = minLevel;
		this.delegate = delegate;
	}

	/**
	 * Default is Trace.
	 */
	public RememberLog() {
		this(Level.Trace);
	}

	public RememberLog(final Logger delegate) {
		this(Level.Trace, delegate);
	}

	@Override
	public String getName() {
		if (this.delegate == null) {
			return "RememberLog";
		}
		assert this.delegate != null;
		return this.delegate.getName();
	}

	@Override
	public void debug(final String msg) {
		if (shouldLog(Level.Debug)) {
			log("debug", msg);
		}
		if (this.delegate != null) {
			this.delegate.debug(msg);
		}
	}

	@Override
	public void debug(final String msg, final Throwable t) {
		if (shouldLog(Level.Debug)) {
			log("debug", msg, t);
		}
		if (this.delegate != null) {
			this.delegate.debug(msg, t);
		}
	}

	@Override
	public void error(final String msg) {
		if (shouldLog(Level.Error)) {
			log("error", msg);
		}
		if (this.delegate != null) {
			this.delegate.error(msg);
		}
	}

	@Override
	public void error(final String msg, final Throwable t) {
		if (shouldLog(Level.Error)) {
			log("error", msg, t);
		}
		if (this.delegate != null) {
			this.delegate.error(msg, t);
		}
	}

	public String getLogs() {
		return this.buf.toString();
	}

	@Override
	public void info(final String msg) {
		if (shouldLog(Level.Info)) {
			log("info", msg);
		}
		if (this.delegate != null) {
			this.delegate.info(msg);
		}
	}

	@Override
	public void info(final String msg, final Throwable t) {
		if (shouldLog(Level.Info)) {
			log("info", msg, t);
		}
		if (this.delegate != null) {
			this.delegate.info(msg, t);
		}
	}

	private void log(final String logLevel, final String msg) {
		this.buf.append("[" + logLevel + "] " + msg + LINEEND);
	}

	private void log(final String logLevel, final String msg, final Throwable t) {
		this.buf.append("[" + logLevel + "] " + msg + LINEEND);
		this.buf.append("Exception: " + SharedExceptionUtils.toString(t) + LINEEND);
	}

	private boolean shouldLog(@NeverNull final Level level) {
		return level.isAsImportantOrEvenMoreImportantThan(this.minLevel);
	}

	@Override
	public void trace(final String msg) {
		if (shouldLog(Level.Trace)) {
			log("trace", msg);
		}
		if (this.delegate != null) {
			this.delegate.trace(msg);
		}
	}

	@Override
	public void trace(final String msg, final Throwable t) {
		if (shouldLog(Level.Trace)) {
			log("trace", msg, t);
		}
		if (this.delegate != null) {
			this.delegate.trace(msg, t);
		}
	}

	@Override
	public void warn(final String msg) {
		if (shouldLog(Level.Warn)) {
			log("warn", msg);
		}
		if (this.delegate != null) {
			this.delegate.warn(msg);
		}
	}

	@Override
	public void warn(final String msg, final Throwable t) {
		if (shouldLog(Level.Warn)) {
			log("warn", msg, t);
		}
		if (this.delegate != null) {
			this.delegate.warn(msg, t);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return shouldLog(Level.Debug);
	}

	@Override
	public boolean isErrorEnabled() {
		return shouldLog(Level.Error);
	}

	@Override
	public boolean isInfoEnabled() {
		return shouldLog(Level.Info);
	}

	@Override
	public boolean isTraceEnabled() {
		return shouldLog(Level.Trace);
	}

	@Override
	public boolean isWarnEnabled() {
		return shouldLog(Level.Warn);
	}

	@Override
	public void setLevel(final Level level) {
		this.minLevel = level;
	}

}
