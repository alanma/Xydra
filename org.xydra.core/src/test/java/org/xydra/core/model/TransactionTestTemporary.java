package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;


public class TransactionTestTemporary extends AbstractTransactionTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
}
