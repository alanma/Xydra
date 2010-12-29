package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;
import org.xydra.core.test.TestLogger;
import org.xydra.core.test.model.state.AbstractStateTest;


public class StoredStateTest extends AbstractStateTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new MemoryStateStore());
	}
	
}
