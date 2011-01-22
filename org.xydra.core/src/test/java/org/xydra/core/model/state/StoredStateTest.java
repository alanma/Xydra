package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;


public class StoredStateTest extends AbstractStateTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new MemoryStateStore());
	}
	
}
