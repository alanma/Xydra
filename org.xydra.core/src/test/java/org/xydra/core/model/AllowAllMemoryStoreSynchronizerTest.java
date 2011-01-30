package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.store.impl.memory.MemoryPersistence;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * the {@link MemoryPersistence}.
 * 
 * @author dscharrer
 */
public class AllowAllMemoryStoreSynchronizerTest extends AbstractAllowAllStoreSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		simpleStore = new MemoryPersistence(XX.toId("repo"));
		AbstractAllowAllStoreSynchronizerTest.init();
	}
	
}
