package org.xydra.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.xydra.server.test.ChangesApiTest;


@Ignore
public class ChangesApiTestMemory extends ChangesApiTest {
	
	@BeforeClass
	public static void init() {
		
		// Backend is selected by init-param "org.xydra.server" in web.xml
		
		ChangesApiTest.init();
	}
	
	@AfterClass
	public static void cleanup() {
		
		ChangesApiTest.cleanup();
		
	}
	
}
