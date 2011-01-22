package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;


public class SynchronizerTestStored extends AbstractAllowAllMemoryStoreSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new MemoryStateStore());
		AbstractAllowAllMemoryStoreSynchronizerTest.init();
	}
	
}
