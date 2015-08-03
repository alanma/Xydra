package org.xydra.store;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
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

		final XId repoId = Base.toId("repo1");

		if (this.store == null) {
			this.store = new DelegatingAllowAllStore(new GaePersistence(repoId));
		}

		this.factory = BaseRuntime.getCommandFactory();

		final XId modelId1 = Base.toId("TestModel1");
		final XId objectId1 = Base.toId("TestObject1");

		final XCommand modelCommand1 = this.factory.createAddModelCommand(repoId, modelId1, true);

		final XCommand objectCommand1 = this.factory.createAddObjectCommand(
				Base.resolveModel(repoId, modelId1), objectId1, true);

		final XCommand[] commands = { modelCommand1, objectCommand1 };

		final XId actorId = Base.toId("actor1");
		this.store.executeCommands(actorId, "bla", commands, new CB<BatchedResult<Long>[]>());
		XydraRuntime.finishRequest();
	}

	@Test
	public void testSetupPersistence() throws Exception {
		log.info("______________ testSetupPersistence ________________");
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
		log.debug("logtest 1-2-3");
		XydraRuntime.init();

		final XId actorId = Base.toId("actor1");
		final XId repoId = Base.toId("repo1");
		final XId modelId1 = Base.toId("TestModel1");
		final XId objectId1 = Base.toId("TestObject1");
		final GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(Base.resolveModel(
				repoId, modelId1));

		this.factory = BaseRuntime.getCommandFactory();
		final XCommand modelCommand1 = this.factory.createAddModelCommand(repoId, modelId1, true);
		final XCommand objectCommand1 = this.factory.createAddObjectCommand(
				Base.resolveModel(repoId, modelId1), objectId1, true);

		final XydraPersistence pers = new GaePersistence(repoId);
		final ModelRevision modelRev = pers.getModelRevision(modelAddressRequest);
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

		final XId repoId = Base.toId("repo1");
		final XId actorId = Base.toId("actor1");
		final XId modelId1 = Base.toId("TestModel1");
		final XId objectId1 = Base.toId("TestObject1");

		final XydraPersistence pers = new GaePersistence(repoId);

		final XCommand modelCommand1 = BaseRuntime.getCommandFactory()
				.createAddModelCommand(repoId, modelId1, true);
		pers.executeCommand(actorId, modelCommand1);

		final XCommand objectCommand1 = BaseRuntime.getCommandFactory().createAddObjectCommand(
				Base.resolveModel(repoId, modelId1), objectId1, true);
		pers.executeCommand(actorId, objectCommand1);
		XydraRuntime.finishRequest();
	}

	static class CB<T> implements Callback<T> {

		@Override
		public void onFailure(final Throwable exception) {
			throw new RuntimeException(exception);
		}

		@Override
		public void onSuccess(final T object) {
			System.out.println("Success " + object);
		}

	}

}
