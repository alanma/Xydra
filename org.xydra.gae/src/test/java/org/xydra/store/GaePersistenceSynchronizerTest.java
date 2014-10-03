package org.xydra.store;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.AbstractPersistenceSynchronizerTest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.InstanceContext;


public class GaePersistenceSynchronizerTest extends AbstractPersistenceSynchronizerTest {
    
    @BeforeClass
    public static void init() {
        LoggerTestHelper.init();
    }
    
    @Override
	@Before
    public void setUp() {
        InstanceContext.clear();
        super.setUp();
    }
    
    @Override
	@After
    public void tearDown() {
        super.tearDown();
    }
    
    @Override
    protected XydraPersistence createPersistence() {
        GaePersistence p = new GaePersistence(XX.toId("repo"));
        p.clear();
        return p;
    }
    
}
