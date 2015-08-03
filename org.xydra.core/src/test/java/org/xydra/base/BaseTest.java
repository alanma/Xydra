package org.xydra.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class BaseTest {

    @Test
    public void testAddRemoveModel() {
        final XydraPersistence persistence = new MemoryPersistence(Base.toId("testrepo"));
        final XWritableRepository repo = new WritableRepositoryOnPersistence(persistence,
                Base.toId("aaa"));
        final XId modelId = Base.toId("user");

        assertFalse(repo.hasModel(modelId));
        repo.createModel(modelId);
        assertTrue(repo.hasModel(modelId));

        final boolean removed = repo.removeModel(modelId);
        assertTrue(removed);

        DumpUtilsBase.dump("aaa", repo);

        assertFalse(repo.hasModel(modelId));
    }

}
