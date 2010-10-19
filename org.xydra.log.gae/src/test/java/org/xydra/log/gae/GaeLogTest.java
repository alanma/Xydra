package org.xydra.log.gae;

import org.junit.BeforeClass;
import org.xydra.log.XydraLogTest;


public class GaeLogTest extends XydraLogTest {
	
	@BeforeClass
	public static void init() {
		GaeLoggerFactorySPI.init();
	}
	
}
