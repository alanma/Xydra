package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.test.TestLogger;
import org.xydra.core.test.model.state.AbstractStateTest;


public class TemporaryStateTest extends AbstractStateTest {
	
	@BeforeClass
	public static void configureXSPI() {
		TestLogger.init();
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
	@Override
	protected boolean canPersist() {
		return false;
	}
	
}
