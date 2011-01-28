package org.xydra.core;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;


public class TestAPIStored extends AbstractTestAPI {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		XSPI.setStateStore(new MemoryStateStore());
	}
	
}
