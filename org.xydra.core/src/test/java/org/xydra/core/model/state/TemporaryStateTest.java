package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;


public class TemporaryStateTest extends AbstractStateTest {
	
	@BeforeClass
	public static void configureXSPI() {
		LoggerTestHelper.init();
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
	@Override
	protected boolean canPersist() {
		return false;
	}
	
}
