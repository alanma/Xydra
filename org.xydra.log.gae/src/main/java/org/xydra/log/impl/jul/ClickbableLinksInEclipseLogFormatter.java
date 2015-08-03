package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.ThreadSafe;

/**
 * A custom java util logging formatter trying to output the pattern
 *
 * <pre>
 * log4j.appender.console=org.apache.log4j.ConsoleAppender
 * log4j.appender.console.layout=org.apache.log4j.PatternLayout
 * log4j.appender.console.layout.ConversionPattern=%-5p: %m at %C.(%F:%L) on
 * %d{ISO8601}%n
 * </pre>
 *
 * from log4j.
 *
 * <h3>Setting this log handler on AppEngine</h3> Put in the
 * <code>jul.properties</code> the lines
 *
 * <pre>
 * java.util.logging.ConsoleHandler.level=FINE
 * java.util.logging.ConsoleHandler.formatter=org.xydra.log.gae.ClickbableLinksInEclipseLogFormatter
 * </pre>
 *
 * @author xamde
 */
@ThreadSafe
public class ClickbableLinksInEclipseLogFormatter extends java.util.logging.Formatter {

	@Override
	public String format(final LogRecord log) {
		return EclipseFormat.format(log, getLineNumber());
	}

	/**
	 * Get the current line number. Slow operation.
	 *
	 * @return int - Current line number.
	 */
	public static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getLineNumber();
	}

}
