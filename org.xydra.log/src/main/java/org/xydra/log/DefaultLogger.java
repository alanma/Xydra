package org.xydra.log;

public class DefaultLogger extends Logger {
	
	public static final String ROOT_LOGGER_NAME = "ROOT";
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
	
	@Override
	public boolean isDebugEnabled() {
		return this.level.compareTo(Level.Debug) <= 0;
	}
	
	@Override
	public boolean isErrorEnabled() {
		return this.level.compareTo(Level.Error) <= 0;
	}
	
	@Override
	public boolean isInfoEnabled() {
		return this.level.compareTo(Level.Info) <= 0;
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.level.compareTo(Level.Trace) <= 0;
	}
	
	@Override
	public boolean isWarnEnabled() {
		return this.level.compareTo(Level.Warn) <= 0;
	}
	
	private void log(Level level, String msg) {
		if(this.lineNumbers) {
			try {
				throw new RuntimeException("marker");
			} catch(RuntimeException e) {
				StackTraceElement stacktrace = e.getStackTrace()[2];
				output(format(stacktrace.getClassName(), stacktrace.getMethodName(),
				        stacktrace.getLineNumber(), level, msg));
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
				output(format(stacktrace.getClassName(), stacktrace.getMethodName(),
				        stacktrace.getLineNumber(), level, msg, t));
			}
		} else {
			output(format(level, msg, t));
		}
	}
	
	private void output(String formattedMessage) {
		System.out.println(formattedMessage);
	}
	
	@Override
	public void trace(String msg) {
		if(!isTraceEnabled())
			return;
		log(Level.Trace, msg);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		if(!isTraceEnabled())
			return;
		log(Level.Trace, msg, t);
	}
	
	@Override
	public void debug(String msg) {
		if(!isDebugEnabled())
			return;
		log(Level.Debug, msg);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		if(!isDebugEnabled())
			return;
		log(Level.Debug, msg, t);
	}
	
	@Override
	public void info(String msg) {
		if(!isInfoEnabled())
			return;
		log(Level.Info, msg);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		if(!isInfoEnabled())
			return;
		log(Level.Info, msg, t);
	}
	
	@Override
	public void warn(String msg) {
		if(!isWarnEnabled())
			return;
		log(Level.Warn, msg);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		if(!isWarnEnabled())
			return;
		log(Level.Warn, msg, t);
	}
	
	@Override
	public void error(String msg) {
		if(!isErrorEnabled())
			return;
		log(Level.Error, msg);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		if(!isErrorEnabled())
			return;
		log(Level.Error, msg, t);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
