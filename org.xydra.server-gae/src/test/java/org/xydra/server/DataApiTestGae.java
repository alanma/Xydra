package org.xydra.server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.test.DataApiTest;


public class DataApiTestGae extends DataApiTest {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		// must also run already before DataApiTest.init
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		DataApiTest.init();
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
