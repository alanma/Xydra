package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;


public class SynchronizeTestTemporary extends AbstractSynchronizeTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
}
