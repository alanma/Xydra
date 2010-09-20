package org.xydra.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xydra.server.test.DataApiTest;


public class DataApiTestMemory extends DataApiTest {
	
	@BeforeClass
	public static void init() {
		
		// Backend is selected by init-param "org.xydra.server" in web.xml
		
		DataApiTest.init();
		
	}
	
	@AfterClass
	public static void cleanup() {
		
		DataApiTest.cleanup();
		
	}
	
}
