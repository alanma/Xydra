package org.xydra.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.state.MockStateStore;
import org.xydra.core.model.state.XSPI;


public class SynchronizeTestState extends AbstractSynchronizeTest {
	
	static MockStateStore store;
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		store = new MockStateStore();
		XSPI.setStateStore(store);
	}
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		try {
			store.checkConsistency();
		} finally {
			store.resetTrans();
		}
	}
	
	@Override
	@After
	public void tearDown() {
		try {
			store.checkConsistency();
		} finally {
			store.resetTrans();
			super.tearDown();
		}
	}
	
}
