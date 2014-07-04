package org.xydra.core.model.impl.memory;

import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.sync.NewSyncer;


/**
 * Test for {@link NewSyncer} that uses the {@link MemoryPersistence}.
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
