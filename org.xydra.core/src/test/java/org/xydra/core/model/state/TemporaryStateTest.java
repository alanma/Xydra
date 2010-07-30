package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.test.model.state.AbstractStateTest;


public class TemporaryStateTest extends AbstractStateTest {
	
	@BeforeClass
	public static void configureXSPI() {
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
	@Override
	protected boolean canPersist() {
		return false;
	}
	
}
