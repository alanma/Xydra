package org.xydra.core;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.test.AbstractTestAPI;


public class TestAPITemporary extends AbstractTestAPI {
	
	@BeforeClass
	public static void init() {
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
}
