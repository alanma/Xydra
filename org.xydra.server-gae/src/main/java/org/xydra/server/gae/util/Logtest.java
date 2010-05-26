package org.xydra.server.gae.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to debug logging problems. Not in /src/test to be available on
 * production server.
 * 
 * @author voelkel
 * 
 */
public class Logtest {
	
	static Logger log = LoggerFactory.getLogger(Logtest.class);
	
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
	
	public static void main(String[] args) {
		Logtest.log();
	}
}
