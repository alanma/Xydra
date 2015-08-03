package org.xydra.log.util.listener;

import org.xydra.annotations.NeverNull;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.Logger.Level;
import org.xydra.log.coreimpl.util.LimitedBuffer;
import org.xydra.log.util.SharedExceptionUtils;

/**
 * Remembers all log input up to a certain memory limit. Keeps always the piece
 * of the earliest and another piece of the latest logs, even if space becomes
 * exhausted.
 *
 * @author xamde
 */
public class RememberLogListener implements ILogListener {

	private static final String LINEEND = " <br/>\n";

	private final LimitedBuffer buf = new LimitedBuffer(100 * 1024, LINEEND);

	private final Level minLevel;

	/**
	 * Default is Trace.
	 */
	public RememberLogListener() {
		this(Level.Trace);
	}

	/**
	 * @param minLevel
	 *            all logs below this level are discarded
	 */
	public RememberLogListener(final Level minLevel) {
		this.minLevel = minLevel;
	}

	@Override
	public void debug(final Logger log, final String msg) {
		if (shouldLog(Level.Debug)) {
			log("debug", log, msg);
		}
	}

	@Override
	public void debug(final Logger log, final String msg, final Throwable t) {
		if (shouldLog(Level.Debug)) {
			log("debug", log, msg, t);
		}
	}

	@Override
	public void error(final Logger log, final String msg) {
		if (shouldLog(Level.Error)) {
			log("error", log, msg);
		}
	}

	@Override
	public void error(final Logger log, final String msg, final Throwable t) {
		if (shouldLog(Level.Error)) {
			log("error", log, msg, t);
		}
	}

	public String getLogs() {
		return this.buf.toString();
	}

	@Override
	public void info(final Logger log, final String msg) {
		if (shouldLog(Level.Info)) {
			log("info", log, msg);
		}
	}

	@Override
	public void info(final Logger log, final String msg, final Throwable t) {
		if (shouldLog(Level.Info)) {
			log("info", log, msg, t);
		}
	}

	private void log(final String logLevel, final Logger log, final String msg) {
		this.buf.append("[" + logLevel + "] " + log.toString() + ">> " + msg + LINEEND);
	}

	private void log(final String logLevel, final Logger log, final String msg, final Throwable t) {
		this.buf.append("[" + logLevel + "] " + log.toString() + ">> " + msg + LINEEND);
		this.buf.append("Exception: " + SharedExceptionUtils.toString(t) + LINEEND);
	}

	private boolean shouldLog(@NeverNull final Level level) {
		return level.isAsImportantOrEvenMoreImportantThan(this.minLevel);
	}

	@Override
	public void trace(final Logger log, final String msg) {
		if (shouldLog(Level.Trace)) {
			log("trace", log, msg);
		}
	}

	@Override
	public void trace(final Logger log, final String msg, final Throwable t) {
		if (shouldLog(Level.Trace)) {
			log("trace", log, msg, t);
		}
	}

	@Override
	public void warn(final Logger log, final String msg) {
		if (shouldLog(Level.Warn)) {
			log("warn", log, msg);
		}
	}

	@Override
	public void warn(final Logger log, final String msg, final Throwable t) {
		if (shouldLog(Level.Warn)) {
			log("warn", log, msg, t);
		}
	}

}
