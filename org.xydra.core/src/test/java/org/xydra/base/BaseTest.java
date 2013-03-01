package org.xydra.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.util.DumpUtils;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class BaseTest {
	
	@Test
	public void testAddRemoveModel() {
		XydraPersistence persistence = new MemoryPersistence(XX.toId("testrepo"));
		XWritableRepository repo = new WritableRepositoryOnPersistence(persistence, XX.toId("aaa"));
		XId modelId = XX.toId("user");
		
		repo.createModel(modelId);
		assertTrue(repo.hasModel(modelId));
		
		boolean removed = repo.removeModel(modelId);
		assertTrue(removed);
		
		DumpUtils.dump("aaa", repo);
		
		assertFalse(repo.hasModel(modelId));
	}
	
}
