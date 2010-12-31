package org.xydra.core.test.model;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.store.impl.memory.AllowAllStore;
import org.xydra.store.impl.memory.XydraNoAccessRightsNoBatchNoAsyncStore;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * a {@link XydraNoAccessRightsNoBatchNoAsyncStore}.
 * 
 * Subclasses should set the model state backend via
 * {@link XSPI#setStateStore(org.xydra.core.model.state.XStateStore)} and set
 * {@link #simpleStore} to a concrete implementation.
 * 
 * @author dscharrer
 */
abstract public class AbstractAllowAllStoreSynchronizerTest extends AbstractSynchronizerTest {
	
	protected static XydraNoAccessRightsNoBatchNoAsyncStore simpleStore;
	
	@BeforeClass
	public static void init() {
		assertNotNull(simpleStore);
		actorId = XX.toId("tester");
		passwordHash = "top secret";
		store = new AllowAllStore(simpleStore);
	}
	
}
