package org.xydra.log.util;

import org.xydra.log.ILogListener;
import org.xydra.log.Logger;


/**
 * An {@link ILogListener} that keeps all log data in a buffer, which it can
 * render in nice HTML to a Writer.
 * 
 * Very helpful to debug an app running on multiple servers, where you never
 * really know an which one you are.
 * 
 * @author xamde
 */
public class HtmlWriterLogListener implements ILogListener {
	
	private static final long MAX_BUFFER_LENGTH = 1024 * 1024;
	StringBuffer buf = new StringBuffer();
	
	@Override
	public void trace(Logger log, String msg) {
		log("TRACE", log, msg);
	}
	
	public String getAndResetBuffer() {
		String s = this.buf.toString();
		this.buf = new StringBuffer();
		return s;
	}
	
	private void log(String level, Logger log, String msg, Throwable ... t) {
		if(this.buf.length() > MAX_BUFFER_LENGTH) {
			this.buf.setLength(0);
			log("META", log, "Buffer cleared");
		}
		if(level.equalsIgnoreCase("warn") || level.equalsIgnoreCase("error")) {
			this.buf.append("<span style='color:red;'>" + level + "</span> ");
		}
		boolean dataAccess = msg.startsWith("|");
		if(dataAccess) {
			this.buf.append("<span style='color:blue';>");
		}
		this.buf.append("<b>");
		this.buf.append(msg);
		this.buf.append("</b>");
		if(dataAccess) {
			this.buf.append("</span>");
		}
		this.buf.append("<span style='font-size: smaller; color: #ccc;'>");
		this.buf.append(" @");
		this.buf.append(lastDotPart(log.toString()));
		this.buf.append("<i>" + level + "</i> ");
		if(t != null && t.length > 0) {
			this.buf.append(" " + t.getClass().getName());
		} else {
		}
		this.buf.append("</span> ");
		this.buf.append("<br/>\n");
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
		log("TRACE", log, msg, t);
	}
	
	@Override
	public void debug(Logger log, String msg) {
		log("DEBUG", log, msg);
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
		log("DEBUG", log, msg, t);
	}
	
	@Override
	public void info(Logger log, String msg) {
		log("INFO", log, msg);
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
		log("INFO", log, msg, t);
	}
	
	@Override
	public void warn(Logger log, String msg) {
		log("WARN", log, msg);
	}
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		log("WARN", log, msg, t);
	}
	
	@Override
	public void error(Logger log, String msg) {
		log("ERROR", log, msg);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		log("ERROR", log, msg, t);
	}
	
	/**
	 * @param s a canonical class-name
	 * @return the last part after a dot, which is, the short class-name
	 */
	public static String lastDotPart(String s) {
		int i = s.lastIndexOf(".");
		if(i > 0) {
			return s.substring(i + 1);
		} else {
			return s;
		}
	}
	
}
