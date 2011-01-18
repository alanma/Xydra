package org.xydra.core.test.model;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * a {@link XydraPersistence}.
 * 
 * Subclasses should set the model state backend via
 * {@link XSPI#setStateStore(org.xydra.core.model.state.XStateStore)} and set
 * {@link #simpleStore} to a concrete implementation.
 * 
 * @author dscharrer
 */
abstract public class AbstractAllowAllStoreSynchronizerTest extends AbstractSynchronizerTest {
	
	protected static XydraPersistence simpleStore;
	
	@BeforeClass
	public static void init() {
		assertNotNull(simpleStore);
		actorId = XX.toId("tester");
		passwordHash = "top secret";
		store = new DelegatingAllowAllStore(simpleStore);
	}
	
}
