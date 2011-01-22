package org.xydra.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.TestStateStore;
import org.xydra.core.model.state.XSPI;


public class SynchronizerTestState extends AbstractAllowAllMemoryStoreSynchronizerTest {
	
	private static TestStateStore stateStore;
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		stateStore = new TestStateStore();
		XSPI.setStateStore(stateStore);
		AbstractAllowAllMemoryStoreSynchronizerTest.init();
	}
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		try {
			stateStore.checkConsistency();
		} finally {
			stateStore.resetTrans();
		}
	}
	
	@Override
	@After
	public void tearDown() {
		try {
			stateStore.checkConsistency();
		} finally {
			stateStore.resetTrans();
			super.tearDown();
		}
	}
	
}
