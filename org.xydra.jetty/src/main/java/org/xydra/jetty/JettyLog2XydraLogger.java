package org.xydra.jetty;

import org.xydra.log.api.Logger.Level;
import org.xydra.log.api.LoggerFactory;

import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.Logger;

/**
 * Unused.
 * 
 * Maps Jettys logging to Xydra.
 * 
 * Unfortunately, all logging is reported as if originating from this class.
 * Log4j can handle this well. So its best to directly map Jetty to slf4j &
 * log4j.
 * 
 * Modelled after
 * root/jetty-util/src/main/java/org/eclipse/jetty/util/log/JavaUtilLog.java
 * 
 * JavaUtilLog is Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 * 
 * @author xamde
 */
public class JettyLog2XydraLogger extends AbstractLogger {

	private static String format(String msg, Object... args) {
		String message = String.valueOf(msg);
		String braces = "{}";
		StringBuilder builder = new StringBuilder();
		int start = 0;
		for (Object arg : args) {
			int bracesIndex = message.indexOf(braces, start);
			if (bracesIndex < 0) {
				builder.append(message.substring(start));
				builder.append(" ");
				builder.append(arg);
				start = message.length();
			} else {
				builder.append(message.substring(start, bracesIndex));
				builder.append(String.valueOf(arg));
				start = bracesIndex + braces.length();
			}
		}
		builder.append(message.substring(start));
		return builder.toString();
	}

	private org.xydra.log.api.Logger log;

	public JettyLog2XydraLogger(String fullname) {

		this.log = LoggerFactory.getThreadSafeLogger(fullname);
	}

	@Override
	public void debug(String msg, Object... args) {
		this.log.debug(msg);
	}

	@Override
	public void debug(String msg, Throwable t) {
		this.log.debug(msg, t);
	}

	@Override
	public void debug(Throwable thrown) {
		this.log.debug("No message", thrown);
	}

	@Override
	public String getName() {
		return this.log.getName();
	}

	@Override
	public void ignore(Throwable ignored) {
		this.log.debug("Ignored this error", ignored);
	}

	@Override
	public void info(String msg, Object... args) {
		this.log.info(format(msg, args));

	}

	@Override
	public void info(Throwable thrown) {
		this.log.info("No message", thrown);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.log.isDebugEnabled();
	}

	@Override
	protected Logger newLogger(String fullname) {
		return new JettyLog2XydraLogger(fullname);
	}

	@Override
	public void setDebugEnabled(boolean enabled) {
		this.log.setLevel(Level.Debug);
	}

	@Override
	public void warn(String msg, Object... args) {
		this.log.warn(format(msg, args));
	}

	@Override
	public void warn(String msg, Throwable thrown) {
		this.log.warn(msg, thrown);
	}

	@Override
	public void warn(Throwable thrown) {
		this.log.warn("No message", thrown);
	}

	@Override
	public void info(String msg, Throwable thrown) {
		this.log.info(msg, thrown);
	}

}
