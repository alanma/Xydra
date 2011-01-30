package org.xydra.core.model;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * a {@link XydraPersistence}.
 * 
 * Subclasses should set protected member to a concrete implementation.
 * 
 * @author dscharrer
 */
abstract public class AbstractPersistenceSynchronizerTest extends AbstractSynchronizerTest {
	
	/**
	 * The {@link XydraPersistence} to be used for testing. Subclasses should
	 * initialize this before {@link #setUp()} gets called.
	 */
	protected static XydraPersistence persistence;
	
	@BeforeClass
	public static void init() {
		assertNotNull(persistence);
		actorId = XX.toId("tester");
		passwordHash = "top secret";
		store = new DelegatingAllowAllStore(persistence);
	}
	
}
