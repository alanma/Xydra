package org.xydra.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.test.TestLogger;
import org.xydra.core.test.model.AbstractChangeTest;
import org.xydra.core.test.model.state.TestStateStore;


public class ChangeTestState extends AbstractChangeTest {
	
	static TestStateStore store;
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		store = new TestStateStore();
		XSPI.setStateStore(store);
	}
	
	@Before
	public void setUp() {
		try {
			store.checkConsistency();
		} finally {
			store.resetTrans();
		}
	}
	
	@After
	public void tearDown() {
		try {
			store.checkConsistency();
		} finally {
			store.resetTrans();
		}
	}
	
}
