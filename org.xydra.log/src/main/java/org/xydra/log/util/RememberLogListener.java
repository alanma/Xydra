package org.xydra.log.util;

import org.xydra.log.ILogListener;
import org.xydra.log.Logger;


/**
 * Remembers all log input up to a certain memory limit. Keeps always the piece
 * of the earliest and another piece of the latest logs, even if space becomes
 * exhausted.
 * 
 * @author xamde
 * 
 */
public class RememberLogListener implements ILogListener {
	
	/* Max 100 KB */
	private static final int MAXLEN = 100 * 1024;
	
	private static final String LINEEND = " <br/>\n";
	
	private StringBuffer buf = new StringBuffer();
	
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
	
	public String getLogs() {
		return this.buf.toString();
	}
	
	@Override
	public void trace(Logger log, String msg) {
		log("trace", log, msg);
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
		log("trace", log, msg, t);
	}
	
	@Override
	public void debug(Logger log, String msg) {
		log("debug", log, msg);
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
		log("debug", log, msg, t);
	}
	
	@Override
	public void info(Logger log, String msg) {
		log("info", log, msg);
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
		log("info", log, msg, t);
	}
	
	@Override
	public void warn(Logger log, String msg) {
		log("warn", log, msg);
	}
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		log("warn", log, msg, t);
	}
	
	@Override
	public void error(Logger log, String msg) {
		log("error", log, msg);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		log("error", log, msg, t);
	}
	
}
