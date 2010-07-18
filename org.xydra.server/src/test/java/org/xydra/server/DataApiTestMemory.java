package org.xydra.server;

import org.junit.BeforeClass;
import org.xydra.server.test.DataApiTest;


public class DataApiTestMemory extends DataApiTest {
	
	@BeforeClass
	public static void init() {
		
		MemoryXydraServer.initializeRepositoryManager();
		
		DataApiTest.init();
	}
	
}
