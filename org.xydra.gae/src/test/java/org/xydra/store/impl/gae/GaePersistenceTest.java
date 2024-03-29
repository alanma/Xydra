package org.xydra.store.impl.gae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.IMemoryModel;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.XydraRuntime;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.xgae.XGae;

public class GaePersistenceTest {

	private static final Logger log = LoggerFactory.getLogger(GaePersistenceTest.class);

	private static final XId ACTOR = XX.toId("tester");

	@Before
	public void setUp() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
	}

	@After
	public void tearDown() {
		XydraRuntime.finishRequest();
	}

	@Test
	public void testQueryIds() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
		final XId repoId = Base.toId("repo-testQueryIds");

		XydraPersistence pers = new GaePersistence(repoId);

		final XId modelId = Base.createUniqueId();
		final XAddress repoAddr = Base.toAddress(pers.getRepositoryId(), null, null, null);
		final XAddress modelAddr = Base.resolveModel(repoAddr, modelId);
		final GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddr);

		assertFalse(pers.hasManagedModel(modelId));
		assertEquals(new ModelRevision(-1L, false), pers.getModelRevision(modelAddressRequest));

		/* Create model */
		pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assertEquals(new ModelRevision(0, true), pers.getModelRevision(modelAddressRequest));
		assertTrue(pers.hasManagedModel(modelId));

		log.info("###   ADD object ");
		final XId objectId = Base.createUniqueId();
		assertFalse(pers.getModelSnapshot(modelAddressRequest).hasObject(objectId));
		/* Create object */
		final long l = pers.executeCommand(ACTOR,
				MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		assert l >= 0 : "" + l;
		log.info("###   Verify revNr ");
		assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
		assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
		assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());

		log.info("###   Clear memcache");
		XGae.get().memcache().clear();

		pers = new GaePersistence(repoId);

		log.info("###   hasModel?");
		assertTrue(pers.hasManagedModel(modelId));
		log.info("###   getSnapshot");
		assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
		final XWritableModel modelSnapshot = pers.getModelSnapshot(modelAddressRequest);
		assertNotNull("snapshot " + modelAddressRequest + " was null", modelSnapshot);
		log.info("###   dumping");
		DumpUtilsBase.dump("modelSnapshot", modelSnapshot);
		assertTrue("model should have object", modelSnapshot.hasObject(objectId));
		assertNotNull(pers.getObjectSnapshot(new GetWithAddressRequest(Base.resolveObject(modelAddr,
				objectId), true)));
		assertTrue(pers.getModelSnapshot(modelAddressRequest).hasObject(objectId));
	}

	@Test
	public void getEmtpyModel() {
		final XId repoId = Base.toId("repo-getEmtpyModel");
		final XydraPersistence pers = new GaePersistence(repoId);
		final XId modelId = Base.toId("model-getEmtpyModel");

		final ModelRevision modelRev = pers.getModelRevision(new GetWithAddressRequest(Base.resolveModel(
				repoId, modelId), true));
		XyAssert.xyAssert(modelRev != null);
		assert modelRev != null;
		XyAssert.xyAssert(!modelRev.modelExists(), "modelExists should be false but rev is "
				+ modelRev + " for " + modelId);

		final WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(pers, ACTOR);
		XyAssert.xyAssert(!repo.hasModel(modelId));

		repo.createModel(modelId);
		assertNotNull(repo.getModel(modelId));
		assertNull(repo.getModel(Base.createUniqueId()));
	}

	@Test
	public void testTrickyRevisionNumbersForModels() {
		final XydraPersistence pers = new GaePersistence(Base.toId("test-repo6"));

		final XId modelId = Base.createUniqueId();
		final XAddress repoAddr = Base.toAddress(pers.getRepositoryId(), null, null, null);
		final XAddress modelAddr = Base.resolveModel(repoAddr, modelId);
		final GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddr);

		assert !pers.getManagedModelIds().contains(modelId);
		assertEquals(new ModelRevision(-1L, false), pers.getModelRevision(modelAddressRequest));

		pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));

		assertEquals(new ModelRevision(0L, true), pers.getModelRevision(modelAddressRequest));
		assert pers.getManagedModelIds().contains(modelId);

		final long result = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createRemoveCommand(repoAddr, XCommand.FORCED, modelId));
		assertTrue(XCommandUtils.success(result));

		assertEquals(new ModelRevision(1L, false), pers.getModelRevision(modelAddressRequest));

		assert !pers.getModelRevision(modelAddressRequest).modelExists();
	}

	@Test
	public void testAddFieldsValuesTwice() {
		XydraPersistence p;

		// prepare commands
		final XStringValue value1 = XV.toValue("value1");
		final XStringValue value2 = XV.toValue("value2");
		XFieldCommand addValue;
		XFieldCommand chgValue;
		XFieldCommand remValue;

		long result;
		/* Scenarios to test: */
		p = preparePersistenceWithModelAndObject("repo1", false);
		addValue = MemoryFieldCommand.createAddCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value1);
		result = p.executeCommand(ACTOR, addValue);
		assertEquals("no field: addValue (fail)", XCommand.FAILED, result);

		p = preparePersistenceWithModelAndObject("repo2", false);
		remValue = MemoryFieldCommand.createRemoveCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED);
		result = p.executeCommand(ACTOR, remValue);
		assertEquals("no field: remValue (fail)", XCommand.FAILED, result);

		p = preparePersistenceWithModelAndObject("repo3", false);
		chgValue = MemoryFieldCommand.createChangeCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value2);
		result = p.executeCommand(ACTOR, chgValue);
		assertEquals("no field: chgValue (fail)", XCommand.FAILED, result);

		/* now with field */

		p = preparePersistenceWithModelAndObject("repo4", true);
		addValue = MemoryFieldCommand.createAddCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value1);
		result = p.executeCommand(ACTOR, addValue);
		assertEquals("field exists: addValue (succ)", 3, result);

		p = preparePersistenceWithModelAndObject("repo5", true);
		remValue = MemoryFieldCommand.createRemoveCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED);
		result = p.executeCommand(ACTOR, remValue);
		assertEquals("field exists: remValue (fail)", XCommand.NOCHANGE, result);

		p = preparePersistenceWithModelAndObject("repo6", true);
		chgValue = MemoryFieldCommand.createChangeCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value2);
		result = p.executeCommand(ACTOR, chgValue);
		assertEquals("field exists: chgValue (succ)", 3, result);

		p = preparePersistenceWithModelAndObject("repo7", true);
		addValue = MemoryFieldCommand.createAddCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value1);
		result = p.executeCommand(ACTOR, addValue);
		assertEquals("field exists: addValue (succ)", 3, result);
		result = p.executeCommand(ACTOR, addValue);
		assertEquals("field exists: addValue (succ), addValue (noChg)", XCommand.NOCHANGE, result);
		chgValue = MemoryFieldCommand.createChangeCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value2);
		result = p.executeCommand(ACTOR, chgValue);
		assertEquals("field exists: addValue (succ), addValue (noChg),chgValue (succ)", 5, result);

		p = preparePersistenceWithModelAndObject("repo8", true);
		addValue = MemoryFieldCommand.createAddCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value1);
		chgValue = MemoryFieldCommand.createChangeCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value2);
		result = p.executeCommand(ACTOR, addValue);
		assertEquals("field exists: addValue (succ)", 3, result);
		result = p.executeCommand(ACTOR, chgValue);
		assertEquals("field exists: addValue (succ), chgValue (succ)", 4, result);
		result = p.executeCommand(ACTOR, chgValue);
		assertEquals("field exists: addValue (succ), chgValue (succ), chgValue (noChg)",
				XCommand.NOCHANGE, result);

		p = preparePersistenceWithModelAndObject("repo9", true);
		addValue = MemoryFieldCommand.createAddCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
				value1);
		remValue = MemoryFieldCommand.createRemoveCommand(
				Base.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED);
		result = p.executeCommand(ACTOR, addValue);
		assertEquals("field exists: addValue (succ)", 3, result);

		assertFalse(
				"field cannot be empty",
				p.getObjectSnapshot(
						new GetWithAddressRequest(Base.resolveObject(p.getRepositoryId(), model1,
								object1))).getField(field1).isEmpty());
		assertEquals(
				"field has value",
				value1,
				p.getObjectSnapshot(
						new GetWithAddressRequest(Base.resolveObject(p.getRepositoryId(), model1,
								object1))).getField(field1).getValue());

		result = p.executeCommand(ACTOR, remValue);
		assertEquals("field exists: addValue (succ), remValue (succ)", 4, result);
	}

	static final XId model1 = XX.toId("model1");
	static final XId object1 = XX.toId("object1");
	static final XId field1 = XX.toId("field1");

	private static XydraPersistence preparePersistenceWithModelAndObject(final String id, final boolean addField) {
		final XydraPersistence p = new GaePersistence(Base.toId(id));
		final WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(p, ACTOR);
		final XWritableModel model = repo.createModel(model1);
		model.createObject(object1);
		if (addField) {
			/* create field */
			final XObjectCommand addFieldCommand = MemoryObjectCommand
					.createAddCommand(Base.resolveObject(p.getRepositoryId(), model1, object1),
							XCommand.FORCED, field1);
			p.executeCommand(ACTOR, addFieldCommand);
		}
		return p;
	}

	@Test
	public void testAddAndRemove() {
		final XydraPersistence pers = new GaePersistence(Base.toId("test-repo3"));

		final XId modelId = Base.createUniqueId();
		final XId objectId = Base.createUniqueId();
		final XAddress repoAddr = Base.toAddress(pers.getRepositoryId(), null, null, null);
		final XAddress modelAddr = Base.resolveModel(repoAddr, modelId);
		final GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddr);

		assert !pers.getManagedModelIds().contains(modelId);
		// action: create model
		pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		// post-conditions:
		assert pers.getManagedModelIds().contains(modelId);
		assert !pers.getModelSnapshot(modelAddressRequest).hasObject(objectId);
		// action: create object in model
		pers.executeCommand(ACTOR, MemoryModelCommand.createAddCommand(modelAddr, true, objectId));

		// post-conditions:
		assert pers.getModelSnapshot(modelAddressRequest).hasObject(objectId);
		assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());

		// action: delete model (implicitly delete object, too)
		long l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createRemoveCommand(repoAddr, 1, modelId));
		// post-conditions:
		assertEquals(2, l);
		assertEquals(2, pers.getModelRevision(modelAddressRequest).revision());
		assertNull(
				"modelsnapshot should be null after repo command remove, but is "
						+ pers.getModelRevision(modelAddressRequest),
				pers.getModelSnapshot(modelAddressRequest));
		assert !pers.getModelRevision(modelAddressRequest).modelExists();

		// action: re-create model
		pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assertEquals(3, pers.getModelRevision(modelAddressRequest).revision());
		assert pers.getModelRevision(modelAddressRequest).modelExists();

		// action: redundantly create model again
		l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l == XCommand.NOCHANGE : l;
		assert pers.getManagedModelIds().contains(modelId);
		assertEquals("nothing changed, so rev should stay the same", 3,
				pers.getModelRevision(modelAddressRequest).revision());
		assert pers.getModelSnapshot(modelAddressRequest) != null;
	}

	@Test
	public void testAddAndRemoveModel() {
		// XydraRuntime.getConfigMap().put(XydraRuntime.PROP_MEMCACHESTATS,
		// "true");
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");

		final XydraPersistence pers = new GaePersistence(Base.toId("test-repo4"));
		final XId modelId = Base.toId("model1");
		final XAddress repoAddr = Base.toAddress(pers.getRepositoryId(), null, null, null);
		final XAddress modelAdd = Base.resolveModel(repoAddr, modelId);
		final GetWithAddressRequest modelAddr = new GetWithAddressRequest(modelAdd);

		// assert absence
		assert !pers.getManagedModelIds().contains(modelId);
		// assert pers.getModelSnapshot(modelAddr) == null;

		// add model
		log.info("\n\n\n=== add\n\n\n");
		long l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0;
		assert pers.getManagedModelIds().contains(modelId);
		assertEquals(0, pers.getModelRevision(modelAddr).revision());

		// remove model
		log.info("\n\n\n=== remove\\n\\n\\n");
		l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createRemoveCommand(repoAddr, 0, modelId));
		assertEquals(1, l);
		assert !pers.getModelRevision(modelAddr).modelExists();
		// assert pers.getModelSnapshot(modelAddr) == null;
		assertEquals(1, pers.getModelRevision(modelAddr).revision());

		// add model
		log.info("\n\n\n=== add again\\n\\n\\n");
		l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0 : l;
		assert pers.getModelRevision(modelAddr).modelExists();
		assert pers.getManagedModelIds().contains(modelId);
		assert pers.getModelSnapshot(modelAddr) != null;
		assertEquals(2, pers.getModelRevision(modelAddr).revision());

		// System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
	}

	public XydraPersistence createPersistence(final XId repositoryId) {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
		// configureLog4j();
		InstanceContext.clear();
		XydraRuntime.init();
		final XydraPersistence p = new GaePersistence(repositoryId);
		assert p.getManagedModelIds().isEmpty();
		return p;
	}

	@Test
	public void testSimpleOperations() {
		final XId repositoryId = Base.toId("testSimpleOperations");
		final XydraPersistence persistence = createPersistence(repositoryId);
		final XId modelId = Base.toId("model1");
		final GetWithAddressRequest getRequest = new GetWithAddressRequest(Base.toAddress(repositoryId,
				modelId, null, null));
		long modelRev;

		modelRev = persistence.getModelRevision(getRequest).revision();
		assertEquals(-1, modelRev);

		/* Add model1 */
		final XRepositoryCommand addModelCmd = BaseRuntime.getCommandFactory().createForcedAddModelCommand(
				repositoryId, modelId);
		long l = persistence.executeCommand(ACTOR, addModelCmd);
		assertTrue("" + l, l == 0);
		modelRev = persistence.getModelRevision(getRequest).revision();
		assertEquals(0, modelRev);

		/* Add same model again */
		l = persistence.executeCommand(ACTOR, addModelCmd);
		assertTrue("" + l, l == XCommand.NOCHANGE);
		modelRev = persistence.getModelRevision(getRequest).revision();
		assertEquals(0, modelRev);

		/* Add object */
		final XId objectId = Base.toId("object1");
		l = persistence.executeCommand(
				ACTOR,
				BaseRuntime.getCommandFactory().createForcedAddObjectCommand(
						Base.resolveModel(repositoryId, modelId), objectId));
		assertEquals(2, l);
		modelRev = persistence.getModelRevision(getRequest).revision();
		assertEquals(2, modelRev);

		/* Get snapshot */
		final XWritableModel snap = persistence.getModelSnapshot(getRequest);
		assertEquals(modelRev, snap.getRevisionNumber());

		final Set<XId> set = org.xydra.index.IndexUtils.toSet(snap.iterator());
		assertEquals(1, set.size());
	}

	@Test
	public void testAddAndRemoveModelWithObject() {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");

		final XydraPersistence pers = new GaePersistence(Base.toId("test-repo5"));
		final XId modelId = Base.toId("model1");
		final XId objectId = Base.toId("object1");
		final XAddress repoAddr = Base.toAddress(pers.getRepositoryId(), null, null, null);
		final XAddress modelAddress = Base.resolveModel(repoAddr, modelId);
		final GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddress);

		final XCommand addObjectCommand = MemoryModelCommand.createAddCommand(modelAddress, true,
				objectId);

		// assert absence
		assert !pers.getManagedModelIds().contains(modelId);

		// add model & object
		long l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0;
		assert pers.getManagedModelIds().contains(modelId);
		assertEquals(0, pers.getModelRevision(modelAddressRequest).revision());
		l = pers.executeCommand(ACTOR, addObjectCommand);
		assert l >= 0;
		assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());

		// remove model
		final XRepositoryCommand modelRemoveCommand = MemoryRepositoryCommand.createRemoveCommand(
				repoAddr, XCommand.SAFE_STATE_BOUND, modelId);
		l = pers.executeCommand(ACTOR, modelRemoveCommand);
		assert l >= 0 : l;
		assert pers.getManagedModelIds().contains(modelId);
		assert !pers.getModelRevision(modelAddressRequest).modelExists();
		// assert pers.getModelSnapshot(modelAddr) == null;
		assertEquals(2, pers.getModelRevision(modelAddressRequest).revision());

		// add model & object
		l = pers.executeCommand(ACTOR,
				MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		assert l >= 0;
		assert pers.getManagedModelIds().contains(modelId);
		assert pers.getModelRevision(modelAddressRequest).modelExists();
		assertEquals(3, pers.getModelRevision(modelAddressRequest).revision());

		l = pers.executeCommand(ACTOR, addObjectCommand);

		assert l >= 0 : "" + l;
		assertEquals(4, pers.getModelRevision(modelAddressRequest).revision());

		final XWritableModel snap = pers.getModelSnapshot(modelAddressRequest);
		assertEquals(4, snap.getRevisionNumber());

		// TODO compare snapshot & revNr directly

		// System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
	}

	@Test
	public void testNewModelExists() {
		final XId model1 = Base.toId("model1");
		final XAddress modelAddr = Base.resolveModel(Base.toAddress("/repo1"), model1);
		final IMemoryModel model = new MemoryModel(Base.toId("actor"), "fooooo", modelAddr);
		assertTrue(model.exists());
	}

}
