package org.xydra.log.coreimpl.sysout;

import java.util.Collection;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NotThreadSafe;
import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.util.SharedExceptionUtils;

/**
 * Logs to System.out.
 * 
 * @author xamde
 */
@NotThreadSafe
@RunsInGWT(true)
public class DefaultLogger implements Logger {

	private static String formatLevel(Level level) {
		return level.name().toUpperCase() + ": ";
	}

	private static String formatLocation(String className, String methodName, int lineNumber) {
		String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
		return " at " + className + " (" + simpleClassName + ".java:" + lineNumber + ")";
	}

	private static String formatThrowable(Throwable t) {
		if (t == null)
			return "(throwable was null)";
		else
			return " " + t.getMessage() + "\n" + SharedExceptionUtils.toString(t);
	}

	private static void output(String formattedMessage) {
		System.out.println(formattedMessage);
	}

	private Level level = Level.Info;

	private boolean lineNumbers = true;

	@CanBeNull
	private Collection<ILogListener> logListeners;

	private String name;

	/**
	 * @param name
	 *            @NeverNull
	 */
	public DefaultLogger(String name) {
		assert name != null;
		this.name = name;
	}

	/**
	 * @param name
	 * @param logListeners
	 */
	public DefaultLogger(String name, Collection<ILogListener> logListeners) {
		this(name);
		this.logListeners = logListeners;
	}

	@Override
	public void debug(String msg) {
		if (!isDebugEnabled())
			return;
		log(Level.Debug, msg);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.debug(this, msg);
			}
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (!isDebugEnabled())
			return;
		log(Level.Debug, msg, t);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.debug(this, msg, t);
			}
	}

	@Override
	public void error(String msg) {
		if (!isErrorEnabled())
			return;
		log(Level.Error, msg);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.error(this, msg);
			}
	}

	@Override
	public void error(String msg, Throwable t) {
		if (!isErrorEnabled())
			return;
		log(Level.Error, msg, t);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.error(this, msg, t);
			}
	}

	private String format(Level level, String msg) {
		return formatLevel(level) + msg + formatLoggername();
	}

	private String format(Level level, String msg, Throwable t) {
		return formatLevel(level) + msg + " " + formatThrowable(t) + formatLoggername();
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

	private String formatLoggername() {
		return " [" + this.name + "]";
	}

	@Override
	public void info(String msg) {
		if (!isInfoEnabled())
			return;
		log(Level.Info, msg);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.info(this, msg);
			}
	}

	@Override
	public void info(String msg, Throwable t) {
		if (!isInfoEnabled())
			return;
		log(Level.Info, msg, t);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.info(this, msg, t);
			}
	}

	@Override
	public boolean isDebugEnabled() {
		return Level.Debug.isAsImportantOrEvenMoreImportantThan(this.level);
	}

	@Override
	public boolean isErrorEnabled() {
		return Level.Error.isAsImportantOrEvenMoreImportantThan(this.level);
	}

	@Override
	public boolean isInfoEnabled() {
		return Level.Info.isAsImportantOrEvenMoreImportantThan(this.level);
	}

	@Override
	public boolean isTraceEnabled() {
		return Level.Trace.isAsImportantOrEvenMoreImportantThan(this.level);
	}

	@Override
	public boolean isWarnEnabled() {
		return Level.Warn.isAsImportantOrEvenMoreImportantThan(this.level);
	}

	private void log(Level level, String msg) {
		if (this.lineNumbers) {
			try {
				throw new RuntimeException("marker");
			} catch (RuntimeException e) {
				StackTraceElement stacktrace = e.getStackTrace()[2];
				output(format(stacktrace.getClassName(), stacktrace.getMethodName(),
						stacktrace.getLineNumber(), level, msg));
			}
		} else {
			output(format(level, msg));
		}
	}

	private void log(Level level, String msg, Throwable t) {
		if (this.lineNumbers) {
			try {
				throw new RuntimeException("marker");
			} catch (RuntimeException e) {
				StackTraceElement stacktrace = e.getStackTrace()[2];
				output(format(stacktrace.getClassName(), stacktrace.getMethodName(),
						stacktrace.getLineNumber(), level, msg, t));
			}
		} else {
			output(format(level, msg, t));
		}
	}

	@Override
	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public void trace(String msg) {
		if (!isTraceEnabled())
			return;
		log(Level.Trace, msg);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.trace(this, msg);
			}
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (!isTraceEnabled())
			return;
		log(Level.Trace, msg, t);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.trace(this, msg, t);
			}
	}

	@Override
	public void warn(String msg) {
		if (!isWarnEnabled())
			return;
		log(Level.Warn, msg);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.warn(this, msg);
			}
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (!isWarnEnabled())
			return;
		log(Level.Warn, msg, t);
		if (this.logListeners != null)
			for (ILogListener l : this.logListeners) {
				l.warn(this, msg, t);
			}
	}

	@Override
	public String getName() {
		return this.name;
	}
}
