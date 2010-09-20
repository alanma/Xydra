package org.xydra.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xydra.server.impl.memory.MemoryXydraServer;
import org.xydra.server.test.PreTest;


public class PreTestMemory extends PreTest {
	
	@BeforeClass
	public static void init() {
		
		XydraServerDefaultConfiguration.setDefaultXydraServer(new MemoryXydraServer());
		
	}
	
	@AfterClass
	public static void cleanup() {
		
		XydraServerDefaultConfiguration.setDefaultXydraServer(null);
		
	}
	
}
