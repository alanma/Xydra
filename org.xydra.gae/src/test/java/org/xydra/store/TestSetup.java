package org.xydra.store;

import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.gae.GaePersistence;

public class TestSetup {

	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
	}

	private static final Logger log = LoggerFactory.getLogger(TestSetup.class);

	private XydraStore store;
	private XCommandFactory factory;

	@Test
	public void testSetup() throws Exception {
		XydraRuntime.init();

		XId repoId = XX.toId("repo1");

		if (this.store == null) {
			this.store = new DelegatingAllowAllStore(new GaePersistence(repoId));
		}

		this.factory = X.getCommandFactory();

		XId modelId1 = XX.toId("TestModel1");
		XId objectId1 = XX.toId("TestObject1");

		XCommand modelCommand1 = this.factory.createAddModelCommand(repoId, modelId1, true);

		XCommand objectCommand1 = this.factory.createAddObjectCommand(
				XX.resolveModel(repoId, modelId1), objectId1, true);

		XCommand[] commands = { modelCommand1, objectCommand1 };

		XId actorId = XX.toId("actor1");
		this.store.executeCommands(actorId, "bla", commands, new CB<BatchedResult<Long>[]>());
		XydraRuntime.finishRequest();
	}

	@Test
	public void testSetupPersistence() throws Exception {
		log.info("______________ testSetupPersistence ________________");
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
		log.debug("logtest 1-2-3");
		XydraRuntime.init();

		XId actorId = XX.toId("actor1");
		XId repoId = XX.toId("repo1");
		XId modelId1 = XX.toId("TestModel1");
		XId objectId1 = XX.toId("TestObject1");
		GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(XX.resolveModel(
				repoId, modelId1));

		this.factory = X.getCommandFactory();
		XCommand modelCommand1 = this.factory.createAddModelCommand(repoId, modelId1, true);
		XCommand objectCommand1 = this.factory.createAddObjectCommand(
				XX.resolveModel(repoId, modelId1), objectId1, true);

		XydraPersistence pers = new GaePersistence(repoId);
		ModelRevision modelRev = pers.getModelRevision(modelAddressRequest);
		log.debug("modelRev = " + modelRev);
		// assertFalse("persistence has just been created",
		// modelRev.modelExists());
		pers.executeCommand(actorId, modelCommand1);
		log.info("rev = " + pers.getModelRevision(modelAddressRequest));
		pers.executeCommand(actorId, objectCommand1);
		log.info("rev = " + pers.getModelRevision(modelAddressRequest));
		XydraRuntime.finishRequest();
	}

	@Test
	public void testSetupPersistenceModelCommand() throws Exception {
		XydraRuntime.init();

		XId repoId = XX.toId("repo1");
		XId actorId = XX.toId("actor1");
		XId modelId1 = XX.toId("TestModel1");
		XId objectId1 = XX.toId("TestObject1");

		XydraPersistence pers = new GaePersistence(repoId);

		XCommand modelCommand1 = X.getCommandFactory()
				.createAddModelCommand(repoId, modelId1, true);
		pers.executeCommand(actorId, modelCommand1);

		XCommand objectCommand1 = X.getCommandFactory().createAddObjectCommand(
				XX.resolveModel(repoId, modelId1), objectId1, true);
		pers.executeCommand(actorId, objectCommand1);
		XydraRuntime.finishRequest();
	}

	static class CB<T> implements Callback<T> {

		@Override
		public void onFailure(Throwable exception) {
			throw new RuntimeException(exception);
		}

		@Override
		public void onSuccess(T object) {
			System.out.println("Success " + object);
		}

	}

}
