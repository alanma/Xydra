package org.xydra.server.impl.gae;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.server.impl.newgae.GaeTestfixer;
import org.xydra.server.impl.newgae.GaeUtils;
import org.xydra.server.test.ChangesApiTest;
import org.xydra.server.test.DataApiTest;


public class ChangesApiTestGae extends ChangesApiTest {
	
	@BeforeClass
	public static void init() {
		
		// Backend is selected by init-param "org.xydra.server" in web.xml
		
		GaeTestfixer.enable();
		ChangesApiTest.init();
		assert !GaeUtils.transactionsActive();
	}
	
	@Override
	@Before
	public void setUp() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		super.setUp();
		assert !GaeUtils.transactionsActive();
	}
	
	@Override
	@After
	public void tearDown() {
		assert !GaeUtils.transactionsActive();
		super.tearDown();
		assert !GaeUtils.transactionsActive();
		GaeTestfixer.tearDown();
	}
	
	@AfterClass
	public static void cleanup() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		DataApiTest.cleanup();
		assert !GaeUtils.transactionsActive();
		GaeTestfixer.tearDown();
	}
	
}
