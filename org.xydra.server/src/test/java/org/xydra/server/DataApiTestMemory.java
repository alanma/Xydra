package org.xydra.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xydra.server.impl.memory.MemoryXydraServer;
import org.xydra.server.test.DataApiTest;


public class DataApiTestMemory extends DataApiTest {
	
	@BeforeClass
	public static void init() {
		
		XydraServerDefaultConfiguration.setDefaultXydraServer(new MemoryXydraServer());
		
		DataApiTest.init();
		
	}
	
	@AfterClass
	public static void cleanup() {
		
		DataApiTest.cleanup();
		
		XydraServerDefaultConfiguration.setDefaultXydraServer(null);
		
	}
	
}
