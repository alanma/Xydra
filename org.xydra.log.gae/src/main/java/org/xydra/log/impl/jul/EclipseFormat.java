package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

public class EclipseFormat {

	/**
	 * The key to click-able links in Eclipse seems to be '(SomeName.java:941)'.
	 *
	 * @param logRecord
	 * @param lineNumber
	 *            use 1 if unknown
	 * @return a nicely formatted message
	 */
	public static String format(final LogRecord logRecord, final int lineNumber) {
		final String level = logRecord.getLevel().getName();
		final String msg = logRecord.getMessage();
		final String loggername = logRecord.getLoggerName();
		final String clazz = Jul_GwtEmul.getSourceClassName(logRecord);
		final String method = Jul_GwtEmul.getSourceMethodName(logRecord);

		final StringBuffer buf = new StringBuffer();
		buf.append(level);
		buf.append(": ");
		buf.append(msg);
		buf.append(" ");
		buf.append(loggername);
		buf.append("#");
		buf.append(method);
		buf.append(" at ");

		// clickable link
		buf.append("(");
		buf.append(clazz);
		buf.append(".java:");
		buf.append(lineNumber);
		buf.append(") ");

		return buf.toString();
	}

}
