package org.xydra.core.model.impl.memory;

import static org.junit.Assert.assertNotNull;

import org.xydra.base.Base;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.sync.NewSyncer;


/**
 * Test for {@link NewSyncer} that uses a {@link XydraPersistence}.
 *
 * Subclasses should set protected member to a concrete implementation.
 *
 * @author dscharrer
 */
public abstract class AbstractPersistenceSynchronizerTest extends AbstractSynchronizerTest {

    protected abstract XydraPersistence createPersistence();

    @Override
    protected XydraStore createStore() {
        final XydraPersistence persistence = createPersistence();
        assertNotNull(persistence);
        this.actorId = Base.toId("tester");
        this.passwordHash = "top secret";
        final XydraStore store = new DelegatingAllowAllStore(persistence);
        return store;
    }

}
