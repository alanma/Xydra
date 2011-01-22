package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;


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
