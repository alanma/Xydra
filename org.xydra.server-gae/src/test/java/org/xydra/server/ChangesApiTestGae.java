package org.xydra.server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.test.ChangesApiTest;
import org.xydra.server.test.DataApiTest;


public class ChangesApiTestGae extends ChangesApiTest {
	
	@BeforeClass
	public static void init() {
		
		// Backend is selected by init-param "org.xydra.server" in web.xml
		
		GaeTestfixer.enable();
		ChangesApiTest.init();
	}
	
	@Override
	@Before
	public void setUp() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		super.setUp();
	}
	
	@Override
	@After
	public void tearDown() {
		super.tearDown();
		GaeTestfixer.tearDown();
	}
	
	@AfterClass
	public static void cleanup() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		DataApiTest.cleanup();
		GaeTestfixer.tearDown();
	}
	
}
