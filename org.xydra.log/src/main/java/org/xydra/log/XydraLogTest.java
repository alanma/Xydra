package org.xydra.log;

import org.junit.Before;
import org.junit.Test;


public class XydraLogTest {
	
	public static Logger log;
	
	@Before
	public void initLogger() {
		log = LoggerFactory.getLogger(XydraLogTest.class);
	}
	
	@Test
	public void simpleTest() {
		if(log.isTraceEnabled()) {
			log.trace("trace");
		}
		log.trace("trace", new RuntimeException("e"));
		
		if(log.isDebugEnabled()) {
			log.debug("debug");
		}
		log.debug("debug", new RuntimeException("e"));
		
		if(log.isInfoEnabled()) {
			log.info("info");
		}
		log.info("info", new RuntimeException("e"));
		
		if(log.isWarnEnabled()) {
			log.warn("warn");
		}
		log.warn("warn", new RuntimeException("e"));
		
		if(log.isErrorEnabled()) {
			log.error("error");
		}
		log.error("error", new RuntimeException("e"));
	}
	
}
