package org.xydra.server.rest.log;

import java.util.logging.LogRecord;


/**
 * A custom java util logging formatter trying to output the pattern
 * 
 * log4j.appender.console=org.apache.log4j.ConsoleAppender
 * 
 * log4j.appender.console.layout=org.apache.log4j.PatternLayout
 * 
 * log4j.appender.console.layout.ConversionPattern=%-5p: %m at %C.(%F:%L) on
 * %d{ISO8601}%n
 * 
 * from log4j. Unfortunately, AppEngine does not allow to set a custom log
 * formatter, yet.
 * 
 * @author voelkel
 * 
 */
public class XamFormatter extends java.util.logging.Formatter {
	
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
