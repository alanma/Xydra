package org.xydra.store;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.AbstractPersistenceSynchronizerTest;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.InstanceContext;


public class GaePersistenceSynchronizerTest extends AbstractPersistenceSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@Before
	public void setUp() {
		InstanceContext.clear();
		XydraRuntime.getMemcache().clear();
		//
		persistence = new GaePersistence(XX.toId("repo"));
		AbstractPersistenceSynchronizerTest.init();
		super.setUp();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
}
