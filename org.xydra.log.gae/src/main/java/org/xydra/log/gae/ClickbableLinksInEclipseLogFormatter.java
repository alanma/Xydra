package org.xydra.log.gae;

import java.util.logging.LogRecord;


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
 * @author voelkel
 */
public class ClickbableLinksInEclipseLogFormatter extends java.util.logging.Formatter {
	
	@Override
	public String format(LogRecord log) {
		String level = log.getLevel().getName();
		String msg = log.getMessage();
		String loggername = log.getLoggerName();
		String clazz = log.getSourceClassName();
		String method = log.getSourceMethodName();
		int lineNumber = getLineNumber();
		
		StringBuffer buf = new StringBuffer();
		buf.append(level);
		buf.append(": ");
		buf.append(msg);
		buf.append(" ");
		buf.append(loggername);
		buf.append("#");
		buf.append(method);
		buf.append(" at ");
		buf.append("(");
		buf.append(clazz);
		buf.append(".java:");
		buf.append(lineNumber);
		buf.append(")\r\n");
		
		return buf.toString();
	}
	
	/**
	 * Get the current line number.
	 * 
	 * @return int - Current line number.
	 */
	public static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getLineNumber();
	}
	
}
