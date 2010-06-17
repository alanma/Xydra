package org.xydra.log;

public class DefaultLogger {
	
	private static enum Level {
		Trace, Debug, Info, Warn, Error;
	}
	
	private Level level = Level.Info;
	private boolean lineNumbers = true;
	private String name;
	
	public DefaultLogger(String name) {
		this.name = name;
	}
	
	private String format(Level level, String msg) {
		return formatLevel(level) + msg + formatLoggername();
	}
	
	private String format(Level level, String msg, Throwable t) {
		return formatLevel(level) + msg + " " + formatThrowable(t) + formatLoggername();
	}
	
	private String formatLevel(Level level) {
		return level.name().toUpperCase() + ": ";
	}
	
	private String formatLoggername() {
		return " [" + this.name + "]";
	}
	
	private String formatThrowable(Throwable t) {
		return " " + t.getMessage() + " " + t.getClass().getName();
	}
	
	private String formatLocation(String className, String methodName, int lineNumber) {
		String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
		return " at " + className + " (" + simpleClassName + ".java:" + lineNumber + ")";
	}
	
	/**
	 * @param className
	 * @param methodName
	 * @param lineNumber
	 * @param level
	 * @param msg
	 * @return a String that turns into a clickable hyperlink in the Eclipse
	 *         console, such as
	 *         "DEBUG: msg at org.example.package.MyClass.(MyClass.java:450)"
	 */
	private String format(String className, String methodName, int lineNumber, Level level,
	        String msg) {
		return formatLevel(level) + msg + formatLocation(className, methodName, lineNumber)
		        + formatLoggername();
	}
	
	private String format(String className, String methodName, int lineNumber, Level level,
	        String msg, Throwable t) {
		return formatLevel(level) + msg + formatThrowable(t)
		        + formatLocation(className, methodName, lineNumber) + formatLoggername();
	}
	
	public boolean isDebugEnabled() {
		return this.level.compareTo(Level.Debug) <= 0;
	}
	
	public boolean isErrorEnabled() {
		return this.level.compareTo(Level.Error) <= 0;
	}
	
	public boolean isInfoEnabled() {
		return this.level.compareTo(Level.Info) <= 0;
	}
	
	public boolean isTraceEnabled() {
		return this.level.compareTo(Level.Trace) <= 0;
	}
	
	public boolean isWarnEnabled() {
		return this.level.compareTo(Level.Warn) <= 0;
	}
	
	private void log(Level level, String msg) {
		if(this.lineNumbers) {
			try {
				throw new RuntimeException("marker");
			} catch(RuntimeException e) {
				StackTraceElement stacktrace = e.getStackTrace()[2];
				output(format(stacktrace.getClassName(), stacktrace.getMethodName(), stacktrace
				        .getLineNumber(), level, msg));
			}
		} else {
			output(format(level, msg));
		}
	}
	
	private void log(Level level, String msg, Throwable t) {
		if(this.lineNumbers) {
			try {
				throw new RuntimeException("marker");
			} catch(RuntimeException e) {
				StackTraceElement stacktrace = e.getStackTrace()[2];
				output(format(stacktrace.getClassName(), stacktrace.getMethodName(), stacktrace
				        .getLineNumber(), level, msg, t));
			}
		} else {
			output(format(level, msg, t));
		}
	}
	
	private void output(String formattedMessage) {
		System.out.println(formattedMessage);
	}
	
	public void trace(String msg) {
		if(!isTraceEnabled())
			return;
		log(Level.Trace, msg);
	}
	
	public void trace(String msg, Throwable t) {
		if(!isTraceEnabled())
			return;
		log(Level.Trace, msg, t);
	}
	
	public void debug(String msg) {
		if(!isDebugEnabled())
			return;
		log(Level.Debug, msg);
	}
	
	public void debug(String msg, Throwable t) {
		if(!isDebugEnabled())
			return;
		log(Level.Debug, msg, t);
	}
	
	public void info(String msg) {
		if(!isInfoEnabled())
			return;
		log(Level.Info, msg);
	}
	
	public void info(String msg, Throwable t) {
		if(!isInfoEnabled())
			return;
		log(Level.Info, msg, t);
	}
	
	public void warn(String msg) {
		if(!isWarnEnabled())
			return;
		log(Level.Warn, msg);
	}
	
	public void warn(String msg, Throwable t) {
		if(!isWarnEnabled())
			return;
		log(Level.Warn, msg, t);
	}
	
	public void error(String msg) {
		if(!isErrorEnabled())
			return;
		log(Level.Error, msg);
	}
	
	public void error(String msg, Throwable t) {
		if(!isErrorEnabled())
			return;
		log(Level.Error, msg, t);
	}
}
