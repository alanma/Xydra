package org.xydra.log;

import org.junit.Test;


public class UseXydraLog {
	
	public static final Logger log = LoggerFactory.getLogger(UseXydraLog.class);
	
	@Test
	public void simpleTest() {
		if(log.isTraceEnabled()) {
			log.trace("42");
		}
		log.trace("t", new RuntimeException("e"));
		
		if(log.isDebugEnabled()) {
			log.debug("42");
		}
		log.debug("t", new RuntimeException("e"));
		
		if(log.isInfoEnabled()) {
			log.info("42");
		}
		log.info("t", new RuntimeException("e"));
		
		if(log.isWarnEnabled()) {
			log.warn("42");
		}
		log.warn("t", new RuntimeException("e"));
		
		if(log.isErrorEnabled()) {
			log.error("42");
		}
		log.error("t", new RuntimeException("e"));
	}
	
}
