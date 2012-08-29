package org.xydra.log;

/**
 * Abstract base class for Xydra loggers
 * 
 * @author voelkel
 */
public abstract class Logger {
	
	/*
	 * The current code in this class is thread safe. Changing this class might
	 * change that and may make implementations of this abstract class, which
	 * are currently thread-safe, thread-unsafe.
	 */
	
	/**
	 * The classic set of warning levels as found in log4j and slf4j.
	 * 
	 * @author voelkel
	 */
	public static enum Level {
		Trace(0), Debug(1), Info(2), Warn(3), Error(4);
		
		Level(int num) {
			this.num = num;
		}
		
		private int num;
		
		public int getNumericLevel() {
			return this.num;
		}
		
		public boolean isAsImportantOrEvenMoreImportantThan(Level other) {
			return this.num >= other.num;
		}
		
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
