package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;
import org.xydra.core.test.model.AbstractChangeTest;


public class ChangeTestStored extends AbstractChangeTest {
	
	@BeforeClass
	public static void init() {
		XSPI.setStateStore(new MemoryStateStore());
	}
	
}
