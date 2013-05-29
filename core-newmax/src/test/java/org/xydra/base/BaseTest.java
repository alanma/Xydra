package org.xydra.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.XX;
import org.xydra.core.util.DumpUtils;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class BaseTest {
    
    @Test
    public void testAddRemoveModel() {
        XydraPersistence persistence = new MemoryPersistence(Base.toId("testrepo"));
        XWritableRepository repo = new WritableRepositoryOnPersistence(persistence,
                Base.toId("aaa"));
        XId modelId = XX.toId("user");
        
        assertFalse(repo.hasModel(modelId));
        repo.createModel(modelId);
        assertTrue(repo.hasModel(modelId));
        
        boolean removed = repo.removeModel(modelId);
        assertTrue(removed);
        
        DumpUtils.dump("aaa", repo);
        
        assertFalse(repo.hasModel(modelId));
    }
    
}
