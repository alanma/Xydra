package org.xydra.log.util;

import org.xydra.annotations.NeverNull;
import org.xydra.log.ILogListener;
import org.xydra.log.Logger;
import org.xydra.log.Logger.Level;


/**
 * Remembers all log input up to a certain memory limit. Keeps always the piece
 * of the earliest and another piece of the latest logs, even if space becomes
 * exhausted.
 * 
 * @author xamde
 * 
 */
public class RememberLogListener implements ILogListener {
	
	private static final String LINEEND = " <br/>\n";
	
	/* Max 100 KB */
	private static final int MAXLEN = 100 * 1024;
	
	private StringBuffer buf = new StringBuffer();
	
	private Level minLevel;
	
	/**
	 * Default is Trace.
	 */
	public RememberLogListener() {
		this(Level.Trace);
	}
	
	/**
	 * @param minLevel all logs below this level are discarded
	 */
	public RememberLogListener(Level minLevel) {
		this.minLevel = minLevel;
	}
	
	@Override
	public void debug(Logger log, String msg) {
		if(shouldLog(Level.Debug))
			log("debug", log, msg);
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
		if(shouldLog(Level.Debug))
			log("debug", log, msg, t);
	}
	
	@Override
	public void error(Logger log, String msg) {
		if(shouldLog(Level.Error))
			log("error", log, msg);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		if(shouldLog(Level.Error))
			log("error", log, msg, t);
	}
	
	public String getLogs() {
		return this.buf.toString();
	}
	
	@Override
	public void info(Logger log, String msg) {
		if(shouldLog(Level.Info))
			log("info", log, msg);
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
		if(shouldLog(Level.Info))
			log("info", log, msg, t);
	}
	
	private void log(String logLevel, Logger log, String msg) {
		this.buf.append("[" + logLevel + "] " + log.toString() + ">> " + msg + LINEEND);
		if(this.buf.length() > MAXLEN) {
			this.buf = new StringBuffer("(too many logs, deleted past)" + LINEEND);
		}
	}
	
	private void log(String logLevel, Logger log, String msg, Throwable t) {
		this.buf.append("[" + logLevel + "] " + log.toString() + ">> " + msg + LINEEND);
		this.buf.append("Exception: " + SharedExceptionUtils.toString(t) + LINEEND);
		if(this.buf.length() > MAXLEN) {
			this.buf = new StringBuffer("(too many logs, deleted past)" + LINEEND);
		}
	}
	
	private boolean shouldLog(@NeverNull Level level) {
		return level.isAsImportantOrEvenMoreImportantThan(this.minLevel);
	}
	
	@Override
	public void trace(Logger log, String msg) {
		if(shouldLog(Level.Trace))
			log("trace", log, msg);
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
		if(shouldLog(Level.Trace))
			log("trace", log, msg, t);
	}
	
	@Override
	public void warn(Logger log, String msg) {
		if(shouldLog(Level.Warn))
			log("warn", log, msg);
	}
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		if(shouldLog(Level.Warn))
			log("warn", log, msg, t);
	}
	
}
