package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.xgae.gaeutils.GaeTestfixer;


public class GaeStoreWriteMethodsTest extends AbstractSecureStoreWriteMethodsTest {
    
    @BeforeClass
    public static void init() {
        LoggerTestHelper.init();
        XydraRuntime.init();
        GaeTestfixer.enable();
    }
    
    @Before
    public void setUp() {
        this.store = null;
        GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
        super.setUp();
    }
    
    @Override
    protected XydraStore createStore() {
        
        if(this.store == null) {
            this.store = GaePersistence.create();
        }
        
        return this.store;
    }
    
    @After
    public void tearDown() {
        SynchronousCallbackWithOneResult<Set<XId>> mids = new SynchronousCallbackWithOneResult<Set<XId>>();
        this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
        assertEquals(SynchronousCallbackWithOneResult.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
        XAddress repoAddr = XX.toAddress(getRepositoryId(), null, null, null);
        for(XId modelId : mids.effect) {
            if(modelId.toString().startsWith("internal--")) {
                continue;
            }
            XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(repoAddr,
                    XCommand.FORCED, modelId);
            this.store.executeCommands(getCorrectUser(), getCorrectUserPasswordHash(),
                    new XCommand[] { removeCommand }, null);
        }
        
        XydraRuntime.finishRequest();
    }
    
}
