package org.xydra.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xydra.server.impl.memory.MemoryXydraServer;
import org.xydra.server.test.ChangesApiTest;


public class ChangesApiTestMemory extends ChangesApiTest {
	
	@BeforeClass
	public static void init() {
		
		XydraServerDefaultConfiguration.setDefaultXydraServer(new MemoryXydraServer());
		
		ChangesApiTest.init();
	}
	
	@AfterClass
	public static void cleanup() {
		
		ChangesApiTest.cleanup();
		
		XydraServerDefaultConfiguration.setDefaultXydraServer(null);
		
	}
	
}
