package org.xydra.server;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.test.PreTest;


public class PreTestGae extends PreTest {
	
	@BeforeClass
	public static void init() {
		
		GaeTestfixer.enable();
		GaeXydraServer.initializeRepositoryManager();
		
	}
	
	@Before
	public void setUp() {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
	}
	
	@Override
	@After
	public void tearDown() {
		
		super.tearDown();
		
		GaeTestfixer.tearDown();
		
	}
	
}
