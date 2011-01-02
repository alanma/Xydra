package org.xydra.server.impl.gae.state;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.model.state.AbstractStateTest;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeStateTest extends AbstractStateTest {
	
	@Override
	@Before
	public void setUp() {
		GaeTestfixer.enable();
		super.setUp();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@After
	public void tearDown() {
		GaeTestfixer.tearDown();
	}
	
	@Test
	public void trivialTest1() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@Test
	public void trivialTest2() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@BeforeClass
	public static void configureXSPI() {
		// configure
		XSPI.setStateStore(new GaeStateStore());
		GaeTestfixer.enable();
	}
	
}
