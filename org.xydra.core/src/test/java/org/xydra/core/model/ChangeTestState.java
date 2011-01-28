package org.xydra.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.state.MockStateStore;
import org.xydra.core.model.state.XSPI;


public class ChangeTestState extends AbstractChangeTest {
	
	static MockStateStore store;
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		store = new MockStateStore();
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
