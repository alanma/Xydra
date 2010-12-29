package org.xydra.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.test.TestLogger;
import org.xydra.core.test.model.AbstractAllowAllMemoryStoreSynchronizerTest;
import org.xydra.core.test.model.state.TestStateStore;


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
