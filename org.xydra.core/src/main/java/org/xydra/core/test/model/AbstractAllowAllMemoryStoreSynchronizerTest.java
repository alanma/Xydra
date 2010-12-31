package org.xydra.core.test.model;

import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.store.impl.memory.MemoryNoAccessRightsNoBatchNoAsyncStore;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * the {@link MemoryNoAccessRightsNoBatchNoAsyncStore}.
 * 
 * Subclasses should set the model state backend via
 * {@link XSPI#setStateStore(org.xydra.core.model.state.XStateStore)}.
 * 
 * @author dscharrer
 */
abstract public class AbstractAllowAllMemoryStoreSynchronizerTest extends
        AbstractAllowAllStoreSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		simpleStore = new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("remote"));
		AbstractAllowAllStoreSynchronizerTest.init();
	}
	
}
