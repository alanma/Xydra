package org.xydra.log.util.listener;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;

/**
 * An {@link ILogListener} that keeps all log data in a buffer, which it can
 * render in nice HTML to a Writer.
 *
 * Very helpful to debug an app running on multiple servers, where you never
 * really know an which one you are.
 *
 * @author xamde
 */
@RunsInGWT(true)
public class HtmlWriterLogListener implements ILogListener {

	private static final long MAX_BUFFER_LENGTH = 1024 * 1024;

	/**
	 * @param s
	 *            a canonical class-name
	 * @return the last part after a dot, which is, the short class-name
	 */
	public static String lastDotPart(final String s) {
		final int i = s.lastIndexOf(".");
		if (i > 0) {
			return s.substring(i + 1);
		} else {
			return s;
		}
	}

	StringBuffer buf = new StringBuffer();

	@Override
	public void debug(final Logger log, final String msg) {
		log("DEBUG", log, msg);
	}

	@Override
	public void debug(final Logger log, final String msg, final Throwable t) {
		log("DEBUG", log, msg, t);
	}

	@Override
	public void error(final Logger log, final String msg) {
		log("ERROR", log, msg);
	}

	@Override
	public void error(final Logger log, final String msg, final Throwable t) {
		log("ERROR", log, msg, t);
	}

	public String getAndResetBuffer() {
		final String s = this.buf.toString();
		this.buf = new StringBuffer();
		return s;
	}

	@Override
	public void info(final Logger log, final String msg) {
		log("INFO", log, msg);
	}

	@Override
	public void info(final Logger log, final String msg, final Throwable t) {
		log("INFO", log, msg, t);
	}

	private void log(final String level, final Logger log, final String msg, final Throwable... t) {
		if (this.buf.length() > MAX_BUFFER_LENGTH) {
			this.buf.setLength(0);
			log("META", log, "Buffer cleared");
		}
		if (level.equalsIgnoreCase("warn") || level.equalsIgnoreCase("error")) {
			this.buf.append("<span style='color:red;'>" + level + "</span> ");
		}
		final boolean dataAccess = msg.startsWith("|");
		if (dataAccess) {
			this.buf.append("<span style='color:blue';>");
		}
		this.buf.append("<b>");
		this.buf.append(msg);
		this.buf.append("</b>");
		if (dataAccess) {
			this.buf.append("</span>");
		}
		this.buf.append("<span style='font-size: smaller; color: #ccc;'>");
		this.buf.append(" @");
		this.buf.append(lastDotPart(log.toString()));
		this.buf.append("<i>" + level + "</i> ");
		if (t != null && t.length > 0) {
			this.buf.append(" " + t.getClass().getName());
		} else {
		}
		this.buf.append("</span> ");
		this.buf.append("<br/>\n");
	}

	@Override
	public void trace(final Logger log, final String msg) {
		log("TRACE", log, msg);
	}

	@Override
	public void trace(final Logger log, final String msg, final Throwable t) {
		log("TRACE", log, msg, t);
	}

	@Override
	public void warn(final Logger log, final String msg) {
		log("WARN", log, msg);
	}

	@Override
	public void warn(final Logger log, final String msg, final Throwable t) {
		log("WARN", log, msg, t);
	}

}
