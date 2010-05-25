package org.xydra.core.model.state;

import org.junit.BeforeClass;
import org.xydra.core.model.state.AbstractStateTest;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;



public class MemoryStateFactoryTest extends AbstractStateTest {
	
	@BeforeClass
	public static void configureXSPI() {
		XSPI.setStateStore(new MemoryStateStore());
	}
	
}
