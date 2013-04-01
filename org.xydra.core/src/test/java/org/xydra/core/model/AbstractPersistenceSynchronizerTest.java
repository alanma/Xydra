package org.xydra.core.model;

import static org.junit.Assert.assertNotNull;

import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.sync.XSynchronizer;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * a {@link XydraPersistence}.
 * 
 * Subclasses should set protected member to a concrete implementation.
 * 
 * @author dscharrer
 */
public abstract class AbstractPersistenceSynchronizerTest extends AbstractSynchronizerTest {
    
    protected abstract XydraPersistence createPersistence();
    
    @Override
    protected XydraStore createStore() {
        XydraPersistence persistence = createPersistence();
        assertNotNull(persistence);
        this.actorId = XX.toId("tester");
        this.passwordHash = "top secret";
        XydraStore store = new DelegatingAllowAllStore(persistence);
        return store;
    }
    
}
