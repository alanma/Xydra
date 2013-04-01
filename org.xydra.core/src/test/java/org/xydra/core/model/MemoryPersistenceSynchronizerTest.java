package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.sync.XSynchronizer;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * the {@link MemoryPersistence}.
 * 
 * @author dscharrer
 * 
 */
public class MemoryPersistenceSynchronizerTest extends AbstractPersistenceSynchronizerTest {
    
    @BeforeClass
    public static void init() {
        LoggerTestHelper.init();
    }
    
    @Override
    protected XydraPersistence createPersistence() {
        return new MemoryPersistence(XX.toId("repo"));
    }
    
}
