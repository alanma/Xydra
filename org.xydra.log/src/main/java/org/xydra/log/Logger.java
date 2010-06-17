package org.xydra.log;

public abstract class Logger {
	
	public static enum Level {
		Trace, Debug, Info, Warn, Error;
	}
	
	public abstract boolean isDebugEnabled();
	
	public abstract boolean isErrorEnabled();
	
	public abstract boolean isInfoEnabled();
	
	public abstract boolean isTraceEnabled();
	
	public abstract boolean isWarnEnabled();
	
	public abstract void trace(String msg);
	
	public abstract void trace(String msg, Throwable t);
	
	public abstract void debug(String msg);
	
	public abstract void debug(String msg, Throwable t);
	
	public abstract void info(String msg);
	
	public abstract void info(String msg, Throwable t);
	
	public abstract void warn(String msg);
	
	public abstract void warn(String msg, Throwable t);
	
	public abstract void error(String msg);
	
	public abstract void error(String msg, Throwable t);
}
