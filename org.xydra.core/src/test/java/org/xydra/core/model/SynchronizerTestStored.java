package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;
import org.xydra.core.test.TestLogger;
import org.xydra.core.test.model.AbstractAllowAllMemoryStoreSynchronizerTest;


public class SynchronizerTestStored extends AbstractAllowAllMemoryStoreSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new MemoryStateStore());
		AbstractAllowAllMemoryStoreSynchronizerTest.init();
	}
	
}
