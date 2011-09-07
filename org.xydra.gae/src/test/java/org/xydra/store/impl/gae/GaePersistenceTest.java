package org.xydra.store.impl.gae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.util.DumpUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class GaePersistenceTest {
	
	private static final Logger log = LoggerFactory.getLogger(GaePersistenceTest.class);
	
	private static final XID ACTOR = XX.toId("tester");
	
	@Before
	public void setUp() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@Test
	public void testQueryIds() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
		
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo"));
		
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		assertEquals(-1, pers.getModelRevision(modelAddr));
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assertEquals(0, pers.getModelRevision(modelAddr));
		log.info("###   ADD object ");
		long l = pers.executeCommand(ACTOR,
		        MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert l >= 0;
		log.info("###   Verify revNr ");
		assertEquals(1, pers.getModelRevision(modelAddr));
		assertEquals(1, pers.getModelRevision(modelAddr));
		assertEquals(1, pers.getModelRevision(modelAddr));
		
		log.info("###   Clear memcache");
		XydraRuntime.getMemcache().clear();
		
		pers = new GaePersistence(XX.toId("test-repo"));
		
		log.info("###   hasModel?");
		assertTrue(pers.hasModel(modelId));
		log.info("###   getSnapshot");
		XWritableModel modelSnapshot = pers.getModelSnapshot(modelAddr);
		assertNotNull("snapshot " + modelAddr + " was null", modelSnapshot);
		log.info("###   dumping");
		DumpUtils.dump("modelSnapshot", modelSnapshot);
		assertTrue("model should have object", modelSnapshot.hasObject(objectId));
		assertNotNull(pers.getObjectSnapshot(XX.resolveObject(modelAddr, objectId)));
		assertTrue(pers.getModelSnapshot(modelAddr).hasObject(objectId));
	}
	
	@Test
	public void getEmtpyModel() {
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo"));
		XID modelId = XX.createUniqueId();
		WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(pers, ACTOR);
		repo.createModel(modelId);
		
		assertNotNull(repo.getModel(modelId));
		
		assertNull(repo.getModel(XX.createUniqueId()));
		
	}
	
	@Test
	public void testAddAndRemove() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
		
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo3"));
		
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		assert !pers.getModelIds().contains(modelId);
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert pers.getModelIds().contains(modelId);
		assert !pers.getModelSnapshot(modelAddr).hasObject(objectId);
		pers.executeCommand(ACTOR, MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert pers.getModelSnapshot(modelAddr).hasObject(objectId);
		
		assertEquals(1, pers.getModelRevision(modelAddr));
		
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, 1, modelId));
		assert pers.getModelSnapshot(modelAddr) == null;
		
		assert !pers.getModelIds().contains(modelId);
		assertEquals(2, pers.getModelRevision(modelAddr));
		
		long l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l == 3 : l;
		assert pers.getModelIds().contains(modelId);
		assertEquals(3, pers.getModelRevision(modelAddr));
		assert pers.getModelSnapshot(modelAddr) != null;
	}
	
	@Test
	public void testAddAndRemoveModel() {
		// XydraRuntime.getConfigMap().put(XydraRuntime.PROP_MEMCACHESTATS,
		// "true");
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
		
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo4"));
		XID modelId = XX.toId("model1");
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		// assert absence
		assert !pers.getModelIds().contains(modelId);
		// assert pers.getModelSnapshot(modelAddr) == null;
		
		// add model
		log.info("\n\n\n=== add\n\n\n");
		long l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0;
		assert pers.getModelIds().contains(modelId);
		assertEquals(0, pers.getModelRevision(modelAddr));
		
		// remove model
		log.info("\n\n\n=== remove\\n\\n\\n");
		l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, 0, modelId));
		assert l >= 0 : l;
		assert !pers.getModelIds().contains(modelId);
		// assert pers.getModelSnapshot(modelAddr) == null;
		assertEquals(1, pers.getModelRevision(modelAddr));
		
		// add model
		log.info("\n\n\n=== add again\\n\\n\\n");
		l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0 : l;
		assert pers.getModelIds().contains(modelId);
		assert pers.getModelSnapshot(modelAddr) != null;
		assertEquals(2, pers.getModelRevision(modelAddr));
		
		// System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
	}
	
	@Test
	public void testAddAndRemoveModelWithObject() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
		
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo5"));
		XID modelId = XX.toId("model1");
		XID objectId = XX.toId("object1");
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		// assert absence
		assert !pers.getModelIds().contains(modelId);
		
		// add model & object
		long l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0;
		assert pers.getModelIds().contains(modelId);
		assertEquals(0, pers.getModelRevision(modelAddr));
		l = pers.executeCommand(ACTOR,
		        MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert l >= 0;
		assertEquals(1, pers.getModelRevision(modelAddr));
		
		// remove model
		l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, -1, modelId));
		assert l >= 0 : l;
		assert !pers.getModelIds().contains(modelId);
		// assert pers.getModelSnapshot(modelAddr) == null;
		assertEquals(2, pers.getModelRevision(modelAddr));
		
		// add model & object
		l = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0;
		assert pers.getModelIds().contains(modelId);
		assertEquals(3, pers.getModelRevision(modelAddr));
		l = pers.executeCommand(ACTOR,
		        MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert l >= 0;
		assertEquals(4, pers.getModelRevision(modelAddr));
		
		XWritableModel snap = pers.getModelSnapshot(modelAddr);
		assertEquals(4, snap.getRevisionNumber());
		
		// FIXME compare snapshot & revNr directly
		
		// System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
	}
	
}
