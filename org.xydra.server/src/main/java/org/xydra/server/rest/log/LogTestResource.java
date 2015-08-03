package org.xydra.server.rest.log;

import java.util.logging.Handler;

import javax.servlet.http.HttpServletResponse;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.server.rest.XydraRestServer;

/**
 * At boot time, logs on all levels to test logging, exposed at '/logtest'
 *
 * @author xamde
 */
public class LogTestResource {

	static Logger log = LoggerFactory.getLogger(LogTestResource.class);

	public static void restless(final Restless restless, final String prefix) {
		restless.addGet(prefix + "/logtest", LogTestResource.class, "init");
	}

	public void init(final HttpServletResponse res) {
		log();
		String result = "result: ";

		final java.util.logging.Logger julLog = java.util.logging.Logger.getLogger("dummy");
		for (final Handler handler : julLog.getHandlers()) {
			result += " HANDLER " + handler.getClass().getName();
		}
		java.util.logging.Logger parent = julLog.getParent();
		int depth = 0;
		while (parent != null && depth < 10) {
			for (final Handler handler : parent.getHandlers()) {
				result += " PARENT-HANDLER " + handler.getClass().getName();
			}
			parent = julLog.getParent();
			depth++;
		}
		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, result);
	}

	public static void log() {
		log.trace("this is trace");
		log.debug("this is debug");
		log.info("this is info");
		log.warn("this is warn");
		log.error("this is error");
		System.out.println("this is sysout");
		System.err.println("this is syserr");

		System.err.println("Log is traceEnabled? " + log.isTraceEnabled());
		System.err.println("Log is debugEnabled? " + log.isDebugEnabled());
		System.err.println("Log is infoEnabled? " + log.isInfoEnabled());
		System.err.println("Log is warnEnabled? " + log.isWarnEnabled());
		System.err.println("Log is errorEnabled? " + log.isErrorEnabled());
	}

	public static void main(final String[] args) {
		LogTestResource.log();
	}

}
