package org.xydra.log.impl.jul.gwt;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.Logger.Level;

import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * Logs to a DIV. Default log level is 'Info', can be set by using the
 * 'loglevel' URL parameter.
 *
 * @author xamde
 *
 */
@RunsInAppEngine(false)
@RunsInGWT(true)
public class DivLogListener implements ILogListener {

	private Logger.Level logLevel = Level.Info;

	public DivLogListener(final FlowPanel hostPanel) {
		this.hostPanel = hostPanel;
		// get log level from url
		final String loglevel = Location.getParameter("loglevel");
		if (loglevel != null) {
			try {
				final Level level = Logger.Level.valueOf(loglevel);
				this.logLevel = level;
			} catch (final Throwable t) {
				log("warn", "DivLogListener", "Error parsing '" + loglevel + "'", t);
			}
		}
	}

	private final FlowPanel hostPanel;

	@Override
	public void trace(final Logger log, final String msg) {
		log("TRACE", log, msg);
	}

	private void log(final String level, final Logger log, final String msg, final Throwable... t) {
		log(level, lastDotPart(log.toString()), msg, t);
	}

	private void log(final String level, final String loggerName, final String msg, final Throwable... t) {
		final StringBuilder b = new StringBuilder();
		if (level.equalsIgnoreCase("warn") || level.equalsIgnoreCase("error")) {
			b.append("<span style='color:red;'>" + level + "</span> ");
		}
		final boolean dataAccess = msg.startsWith("|");
		if (dataAccess) {
			b.append("<span style='color:blue';>");
		}
		b.append("<b>");
		b.append(msg);
		b.append("</b>");
		if (dataAccess) {
			b.append("</span>");
		}
		b.append("<span style='font-size: smaller; color: #ccc;'>");
		b.append(" @");
		b.append(loggerName);
		b.append("<i>" + level + "</i> ");
		if (t != null && t.length > 0) {
			b.append(" " + t.getClass().getName());
		} else {
		}
		b.append("</span> ");
		b.append("<br/>\n");

		appendAsInlineHtml(b.toString());
	}

	public void appendAsInlineHtml(final String s) {
		this.hostPanel.add(new InlineHTML("" + s));
	}

	@Override
	public void trace(final Logger log, final String msg, final Throwable t) {
		if (Level.Trace.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("TRACE", log, msg, t);
		}
	}

	@Override
	public void debug(final Logger log, final String msg) {
		if (Level.Debug.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("DEBUG", log, msg);
		}
	}

	@Override
	public void debug(final Logger log, final String msg, final Throwable t) {
		if (Level.Debug.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("DEBUG", log, msg, t);
		}
	}

	@Override
	public void info(final Logger log, final String msg) {
		if (Level.Info.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("INFO", log, msg);
		}
	}

	@Override
	public void info(final Logger log, final String msg, final Throwable t) {
		if (Level.Info.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("INFO", log, msg, t);
		}
	}

	@Override
	public void warn(final Logger log, final String msg) {
		if (Level.Warn.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("WARN", log, msg);
		}
	}

	@Override
	public void warn(final Logger log, final String msg, final Throwable t) {
		if (Level.Warn.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("WARN", log, msg, t);
		}
	}

	@Override
	public void error(final Logger log, final String msg) {
		if (Level.Error.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("ERROR", log, msg);
		}
	}

	@Override
	public void error(final Logger log, final String msg, final Throwable t) {
		if (Level.Error.isAsImportantOrEvenMoreImportantThan(this.logLevel)) {
			log("ERROR", log, msg, t);
		}
	}

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

}
