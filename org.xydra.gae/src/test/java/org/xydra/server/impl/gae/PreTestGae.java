package org.xydra.server.impl.gae;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.server.impl.gae.GaeXydraServer;
import org.xydra.server.test.PreTest;
import org.xydra.store.impl.gae.GaeTestfixer;


public class PreTestGae extends PreTest {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		xydraServer = new GaeXydraServer();
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
