package org.xydra.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.RWCachingRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.Clock;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public abstract class AbstractPersistencePerformanceTest {
	
	private static final Logger log = LoggerFactory
	        .getLogger(AbstractPersistencePerformanceTest.class);
	
	private static XID actorId = XX.toId("actor1");
	private static int x = 3;
	
	@Before
	public void setUp() {
		log.info("Creating fresh persistence");
	}
	
	public abstract XydraPersistence createPersistence(XID repositoryId);
	
	@Test
	public void testPerformanceDirect() {
		XID repositoryId = XX.toId("testPerformanceDirect");
		XydraPersistence persistence = createPersistence(repositoryId);
		Clock c = new Clock().start();
		XID modelId = XX.toId("model1");
		XRepositoryCommand addModelCmd = X.getCommandFactory().createForcedAddModelCommand(
		        repositoryId, modelId);
		c.stopAndStart("create-cmd");
		
		long l = persistence.executeCommand(actorId, addModelCmd);
		assertTrue("" + l, l >= 0);
		c.stopAndStart("add-model");
		
		l = persistence.executeCommand(actorId, addModelCmd);
		assertTrue("" + l, l < 0);
		c.stopAndStart("add-model-again");
		
		createObjects(persistence, repositoryId, modelId, x);
		c.stopAndStart("add-" + x + "-objects");
		XWritableModel snap = persistence.getModelSnapshot(XX.toAddress(repositoryId, modelId,
		        null, null));
		
		c.stopAndStart("get-modelsnapshot");
		snap = persistence.getModelSnapshot(XX.toAddress(repositoryId, modelId, null, null));
		c.stopAndStart("get-modelsnapshot2");
		Set<XID> set = org.xydra.index.IndexUtils.toSet(snap.iterator());
		assertEquals(x, set.size());
		log.info(c.getStats());
	}
	
	private static void createObjects(XydraPersistence persistence, XID repositoryId, XID modelId,
	        int ocount) {
		for(int i = 0; i < ocount; i++) {
			XID objectId = XX.toId("object" + i);
			long l = persistence.executeCommand(actorId, X.getCommandFactory()
			        .createForcedAddObjectCommand(repositoryId, modelId, objectId));
			assertTrue(l >= 0);
		}
	}
	
	private static void createObjects(XWritableModel model, int ocount) {
		for(int i = 0; i < ocount; i++) {
			XID objectId = XX.toId("object" + i);
			XWritableObject object = model.createObject(objectId);
			assert objectId.equals(object.getID());
			assert model.hasObject(objectId);
		}
	}
	
	@Test
	public void testPerformanceIndirect() {
		XID repositoryId = XX.toId("testPerformanceIndirect");
		XydraPersistence persistence = createPersistence(repositoryId);
		Clock c = new Clock().start();
		
		WritableRepositoryOnPersistence baseRepository = new WritableRepositoryOnPersistence(
		        persistence, actorId);
		RWCachingRepository repo = new RWCachingRepository(baseRepository, false);
		
		XID modelId = XX.toId("model1");
		XWritableModel model = repo.createModel(modelId);
		c.stopAndStart("add-model");
		
		model = repo.createModel(modelId);
		c.stopAndStart("add-model-again");
		assertEquals("model has just been created, empty", 0,
		        org.xydra.index.IndexUtils.toSet(model.iterator()).size());
		
		createObjects(model, x);
		c.stopAndStart("add-" + x + "-objects");
		assertEquals(x, org.xydra.index.IndexUtils.toSet(model.iterator()).size());
		
		int result = RWCachingRepository.commit(repo, actorId);
		assertEquals(200, result);
		c.stopAndStart("commit");
		
		XAddress modelAddress = XX.toAddress(repositoryId, modelId, null, null);
		long rev = persistence.getModelRevision(modelAddress).revision();
		assertEquals("0=createModel,1=txnWithXObjects" + rev, 1, rev);
		
		XWritableModel snap = persistence.getModelSnapshot(modelAddress);
		assert snap != null;
		Set<XID> set = org.xydra.index.IndexUtils.toSet(snap.iterator());
		assertEquals(x, set.size());
		
		c.stopAndStart("get-modelsnapshot");
		snap = persistence.getModelSnapshot(XX.toAddress(repositoryId, modelId, null, null));
		c.stopAndStart("get-modelsnapshot2");
		set = org.xydra.index.IndexUtils.toSet(snap.iterator());
		assertEquals(x, set.size());
		log.info(c.getStats());
	}
	
}
