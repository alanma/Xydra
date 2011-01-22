package org.xydra.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.TestStateStore;
import org.xydra.core.model.state.XSPI;


public class TransactionTestState extends AbstractTransactionTest {
	
	static TestStateStore store;
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		store = new TestStateStore();
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
