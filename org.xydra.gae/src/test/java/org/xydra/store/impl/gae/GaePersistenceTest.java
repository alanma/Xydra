package org.xydra.store.impl.gae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.util.DumpUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.store.RevisionState;
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
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
	}
	
	@Test
	public void testQueryIds() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
		
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo-1"));
		
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		assertFalse(pers.hasModel(modelId));
		assertEquals(new RevisionState(-1L, false), pers.getModelRevision(modelAddr));
		
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assertEquals(0, pers.getModelRevision(modelAddr).revision());
		assertTrue(pers.hasModel(modelId));
		
		log.info("###   ADD object ");
		RevisionState r = pers.executeCommand(ACTOR,
		        MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert r.revision() >= 0;
		log.info("###   Verify revNr ");
		assertEquals(1, pers.getModelRevision(modelAddr).revision());
		assertEquals(1, pers.getModelRevision(modelAddr).revision());
		assertEquals(1, pers.getModelRevision(modelAddr).revision());
		
		log.info("###   Clear memcache");
		XydraRuntime.getMemcache().clear();
		
		pers = new GaePersistence(XX.toId("test-repo-1"));
		
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
	public void testTrickyRevisionNumbersForModels() {
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo6"));
		
		XID modelId = XX.createUniqueId();
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		assert !pers.getModelIds().contains(modelId);
		assertEquals(new RevisionState(-1L, false), pers.getModelRevision(modelAddr));
		
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		
		assertEquals(new RevisionState(0L, true), pers.getModelRevision(modelAddr));
		assert pers.getModelIds().contains(modelId);
		
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, -1, modelId));
		
		assertEquals(new RevisionState(1L, false), pers.getModelRevision(modelAddr));
		assert !pers.getModelIds().contains(modelId);
	}
	
	@Test
	public void testAddAndRemove() {
		XydraPersistence pers = new GaePersistence(XX.toId("test-repo3"));
		
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		assert !pers.getModelIds().contains(modelId);
		// action: create model
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		// post-conditions:
		assert pers.getModelIds().contains(modelId);
		assert !pers.getModelSnapshot(modelAddr).hasObject(objectId);
		// action: create object in model
		pers.executeCommand(ACTOR, MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		
		// post-conditions:
		assert pers.getModelSnapshot(modelAddr).hasObject(objectId);
		assertEquals(1, pers.getModelRevision(modelAddr).revision());
		
		// action: delete model (implicitly delete object, too)
		RevisionState pair = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, 1, modelId));
		// post-conditions:
		assertEquals(false, pair.modelExists());
		assertEquals(2, pair.revision());
		assertEquals(2, pers.getModelRevision(modelAddr).revision());
		assertNull(
		        "modelsnapshot should be null after repo command remove, but is "
		                + pers.getModelRevision(modelAddr), pers.getModelSnapshot(modelAddr));
		assert !pers.getModelIds().contains(modelId);
		
		// action: re-create model
		pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assertEquals(3, pers.getModelRevision(modelAddr).revision());
		
		// action: redundantly create model again
		pair = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert pair.revision() == XCommand.NOCHANGE : pair;
		assert pers.getModelIds().contains(modelId);
		assertEquals(4, pers.getModelRevision(modelAddr).revision());
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
		RevisionState revState = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert revState.revision() >= 0;
		assert pers.getModelIds().contains(modelId);
		assertEquals(0, pers.getModelRevision(modelAddr).revision());
		
		// remove model
		log.info("\n\n\n=== remove\\n\\n\\n");
		revState = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, 0, modelId));
		assertEquals(1, revState.revision());
		assert !pers.getModelIds().contains(modelId);
		// assert pers.getModelSnapshot(modelAddr) == null;
		assertEquals(1, pers.getModelRevision(modelAddr).revision());
		
		// add model
		log.info("\n\n\n=== add again\\n\\n\\n");
		revState = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert revState.revision() >= 0 : revState;
		assert pers.getModelIds().contains(modelId);
		assert pers.getModelSnapshot(modelAddr) != null;
		assertEquals(2, pers.getModelRevision(modelAddr).revision());
		
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
		RevisionState pair = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert pair.revision() >= 0;
		assert pers.getModelIds().contains(modelId);
		assertEquals(0, pers.getModelRevision(modelAddr).revision());
		pair = pers.executeCommand(ACTOR,
		        MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert pair.revision() >= 0;
		assertEquals(1, pers.getModelRevision(modelAddr).revision());
		
		// remove model
		pair = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createRemoveCommand(repoAddr, -1, modelId));
		assert pair.revision() >= 0 : pair;
		assert !pers.getModelIds().contains(modelId);
		// assert pers.getModelSnapshot(modelAddr) == null;
		assertEquals(2, pers.getModelRevision(modelAddr).revision());
		
		// add model & object
		pair = pers.executeCommand(ACTOR,
		        MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert pair.revision() >= 0;
		assert pers.getModelIds().contains(modelId);
		assertEquals(3, pers.getModelRevision(modelAddr).revision());
		pair = pers.executeCommand(ACTOR,
		        MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert pair.revision() >= 0;
		assertEquals(4, pers.getModelRevision(modelAddr).revision());
		
		XWritableModel snap = pers.getModelSnapshot(modelAddr);
		assertEquals(4, snap.getRevisionNumber());
		
		// FIXME compare snapshot & revNr directly
		
		// System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
	}
	
}
