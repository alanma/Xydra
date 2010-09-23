package org.xydra.server.rest.log;

import java.util.logging.Handler;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.server.rest.XydraRestServer;


public class LogTestResource {
	
	static Logger slf4jLog = LoggerFactory.getLogger(LogTestResource.class);
	
	public static void restless(Restless restless, String prefix) {
		restless.addGet(prefix + "/logtest", LogTestResource.class, "init");
	}
	
	public void init(HttpServletResponse res) {
		log();
		String result = "result: ";
		
		java.util.logging.Logger julLog = java.util.logging.Logger.getLogger("dummy");
		for(Handler handler : julLog.getHandlers()) {
			result += " HANDLER " + handler.getClass().getName();
		}
		java.util.logging.Logger parent = julLog.getParent();
		int depth = 0;
		while(parent != null && depth < 10) {
			for(Handler handler : parent.getHandlers()) {
				result += " PARENT-HANDLER " + handler.getClass().getName();
			}
			parent = julLog.getParent();
			depth++;
		}
		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, result);
	}
	
	public static void log() {
		slf4jLog.trace("this is trace");
		slf4jLog.debug("this is debug");
		slf4jLog.info("this is info");
		slf4jLog.warn("this is warn");
		slf4jLog.error("this is error");
		System.out.println("this is sysout");
		System.err.println("this is syserr");
		
		System.err.println("Log is traceEnabled? " + slf4jLog.isTraceEnabled());
		System.err.println("Log is debugEnabled? " + slf4jLog.isDebugEnabled());
		System.err.println("Log is infoEnabled? " + slf4jLog.isInfoEnabled());
		System.err.println("Log is warnEnabled? " + slf4jLog.isWarnEnabled());
		System.err.println("Log is errorEnabled? " + slf4jLog.isErrorEnabled());
	}
	
	public static void main(String[] args) {
		LogTestResource.log();
	}
	
}
