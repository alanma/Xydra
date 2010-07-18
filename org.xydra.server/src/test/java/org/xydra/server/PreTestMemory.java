package org.xydra.server;

import org.junit.BeforeClass;
import org.xydra.server.test.PreTest;


public class PreTestMemory extends PreTest {
	
	@BeforeClass
	public static void init() {
		
		MemoryXydraServer.initializeRepositoryManager();
		
	}
	
}
