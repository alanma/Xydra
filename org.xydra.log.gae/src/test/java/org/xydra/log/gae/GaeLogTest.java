package org.xydra.log.gae;

import org.junit.BeforeClass;
import org.xydra.log.UseXydraLog;


public class GaeLogTest extends UseXydraLog {
	
	@BeforeClass
	public static void init() {
		GaeLoggerFactorySPI.init();
	}
	
}
