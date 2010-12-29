package org.xydra.core;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;
import org.xydra.core.test.AbstractTestAPI;
import org.xydra.core.test.TestLogger;


public class TestAPIStored extends AbstractTestAPI {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new MemoryStateStore());
	}
	
}
