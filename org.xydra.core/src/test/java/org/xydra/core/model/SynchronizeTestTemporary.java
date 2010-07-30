package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.test.model.AbstractSynchronizeTest;


public class SynchronizeTestTemporary extends AbstractSynchronizeTest {
	
	@BeforeClass
	public static void init() {
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
}
