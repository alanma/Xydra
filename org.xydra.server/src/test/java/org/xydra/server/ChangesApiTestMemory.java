package org.xydra.server;

import org.junit.BeforeClass;
import org.xydra.server.test.ChangesApiTest;


public class ChangesApiTestMemory extends ChangesApiTest {
	
	@BeforeClass
	public static void init() {
		
		MemoryXydraServer.initializeRepositoryManager();
		
		ChangesApiTest.init();
	}
	
}
