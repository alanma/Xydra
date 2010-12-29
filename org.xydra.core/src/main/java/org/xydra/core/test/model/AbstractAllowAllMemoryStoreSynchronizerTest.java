package org.xydra.core.test.model;

import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.store.impl.memory.MemoryNoAccessRightsNoBatchNoAsyncStore;


abstract public class AbstractAllowAllMemoryStoreSynchronizerTest extends
        AbstractAllowAllStoreSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		simpleStore = new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("remote"));
		AbstractAllowAllStoreSynchronizerTest.init();
	}
	
}
