package org.xydra.log;

import org.junit.Before;
import org.junit.Test;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.util.ClassPathTool;

public class XydraLogTest {

	public static Logger log;

	@Before
	public void initLogger() {
		log = LoggerFactory.getLogger(XydraLogTest.class);
	}

	@Test
	public void simpleTest() {
		if (log.isTraceEnabled()) {
			log.trace("trace");
		}
		log.trace("trace", new RuntimeException("test exception at trace level"));

		if (log.isDebugEnabled()) {
			log.debug("debug");
		}
		log.debug("debug", new RuntimeException("test exception at debug level"));

		if (log.isInfoEnabled()) {
			log.info("info");
		}
		log.info("info", new RuntimeException("test exception at info level"));

		if (log.isWarnEnabled()) {
			log.warn("warn");
		}
		log.warn("warn", new RuntimeException("test exception at warn level"));

		if (log.isErrorEnabled()) {
			log.error("error");
		}
		log.error("error", new RuntimeException("test exception at error level"));
	}

	public static void main(final String[] args) {
		ClassPathTool.dumpCurrentClasspath();
	}

}
