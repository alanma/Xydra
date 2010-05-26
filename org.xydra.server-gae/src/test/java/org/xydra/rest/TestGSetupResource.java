package org.xydra.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.rest.GSetupResource;
import org.xydra.server.gae.GaeTestfixer;



public class TestGSetupResource {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
	}
	
	@Before
	public void setUp() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@After
	public void tearDown() {
		GaeTestfixer.tearDown();
	}
	
	@Test
	public void testInit() {
		GSetupResource gSetupResource = new GSetupResource();
		gSetupResource.init();
	}
}
