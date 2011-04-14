package org.xydra.store.impl.gae;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


public class GaePersistenceTest {
	
	private static final XID ACTOR = XX.toId("tester");
	
	@Test
	public void testQueryIds() {
		
		LoggerTestHelper.init();
		
		GaeTestfixer.enable();
		
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo"));
		
		XID modelId = XX.createUniqueID();
		XID objectId = XX.createUniqueID();
		
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		pers.executeCommand(ACTOR, MemoryRepositoryCommand
		        .createAddCommand(repoAddr, true, modelId));
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		pers.executeCommand(ACTOR, MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		
		XydraRuntime.getMemcache().clear();
		
		pers = new GaePersistence(XX.toId("test-repo"));
		
		assertTrue(pers.hasModel(modelId));
		assertNotNull(pers.getModelSnapshot(modelAddr));
		assertNotNull(pers.getObjectSnapshot(XX.resolveObject(modelAddr, objectId)));
		
		assertTrue(pers.getModelSnapshot(modelAddr).hasObject(objectId));
		
	}
}
