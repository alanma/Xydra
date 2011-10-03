package org.xydra.store;

import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class TestSetup {
	
	private XydraStore store;
	private XCommandFactory factory;
	
	@Test
	public void testSetup() throws Exception {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.init();
		
		XID repoId = XX.toId("repo1");
		
		if(this.store == null) {
			this.store = new DelegatingAllowAllStore(new GaePersistence(repoId));
		}
		
		this.factory = X.getCommandFactory();
		
		XID modelId1 = XX.toId("TestModel1");
		XID objectId1 = XX.toId("TestObject1");
		
		XCommand modelCommand1 = this.factory.createAddModelCommand(repoId, modelId1, true);
		
		XCommand objectCommand1 = this.factory.createAddObjectCommand(repoId, modelId1, objectId1,
		        true);
		
		XCommand[] commands = { modelCommand1, objectCommand1 };
		
		XID actorId = XX.toId("actor1");
		this.store.executeCommands(actorId, "bla", commands, new CB<BatchedResult<Long>[]>());
		XydraRuntime.finishRequest();
	}
	
	@Test
	public void testSetupPersistence() throws Exception {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.init();
		
		XID repoId = XX.toId("repo1");
		
		XydraPersistence pers = new GaePersistence(repoId);
		
		this.factory = X.getCommandFactory();
		
		XID modelId1 = XX.toId("TestModel1");
		XID objectId1 = XX.toId("TestObject1");
		
		XCommand modelCommand1 = this.factory.createAddModelCommand(repoId, modelId1, true);
		
		XCommand objectCommand1 = this.factory.createAddObjectCommand(repoId, modelId1, objectId1,
		        true);
		
		XID actorId = XX.toId("actor1");
		pers.executeCommand(actorId, modelCommand1);
		pers.executeCommand(actorId, objectCommand1);
		XydraRuntime.finishRequest();
	}
	
	@Test
	public void testSetupPersistenceModelCommand() throws Exception {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.init();
		
		XID repoId = XX.toId("repo1");
		XID actorId = XX.toId("actor1");
		XID modelId1 = XX.toId("TestModel1");
		XID objectId1 = XX.toId("TestObject1");
		
		XydraPersistence pers = new GaePersistence(repoId);
		
		XCommand modelCommand1 = X.getCommandFactory()
		        .createAddModelCommand(repoId, modelId1, true);
		pers.executeCommand(actorId, modelCommand1);
		
		XCommand objectCommand1 = X.getCommandFactory().createAddObjectCommand(repoId, modelId1,
		        objectId1, true);
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
