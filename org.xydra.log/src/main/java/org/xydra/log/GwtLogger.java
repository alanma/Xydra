package org.xydra.log;

import com.allen_sauer.gwt.log.client.Log;


public class GwtLogger extends Logger {
	
	private String name;
	
	public GwtLogger(String name) {
		this.name = name;
	}
	
	@Override
	public void debug(String msg) {
		Log.debug(this.name + " " + msg);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		Log.debug(this.name + " " + msg, t);
	}
	
	@Override
	public void error(String msg) {
		Log.error(this.name + " " + msg);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		Log.error(this.name + " " + msg, t);
	}
	
	@Override
	public void info(String msg) {
		Log.error(this.name + " " + msg);
	}
	
	@Override
	public void info(String msg, Throwable t) {
		Log.error(this.name + " " + msg, t);
	}
	
	@Override
	public boolean isDebugEnabled() {
		return Log.isDebugEnabled();
	}
	
	@Override
	public boolean isErrorEnabled() {
		return Log.isErrorEnabled();
	}
	
	@Override
	public boolean isInfoEnabled() {
		return Log.isInfoEnabled();
	}
	
	@Override
	public boolean isTraceEnabled() {
		return Log.isTraceEnabled();
	}
	
	@Override
	public boolean isWarnEnabled() {
		return Log.isWarnEnabled();
	}
	
	@Override
	public void trace(String msg) {
		Log.trace(this.name + " " + msg);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		Log.trace(this.name + " " + msg, t);
	}
	
	@Override
	public void warn(String msg) {
		Log.warn(this.name + " " + msg);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		Log.warn(this.name + " " + msg, t);
	}
	
}
