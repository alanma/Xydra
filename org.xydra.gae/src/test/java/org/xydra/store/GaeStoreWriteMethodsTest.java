package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeStoreWriteMethodsTest extends AbstractSecureStoreWriteMethodsTest {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		LoggerTestHelper.init();
		XydraRuntime.init();
	}
	
	@Before
	public void before() {
		this.store = null;
		super.before();
	}
	
	@After
	public void after() {
		XydraRuntime.finishRequest();
	}
	
	@Override
	protected XydraStore getStore() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		if(this.store == null) {
			this.store = GaePersistence.get();
		}
		
		return this.store;
	}
	
	@After
	public void tearDown() {
		SynchronousCallbackWithOneResult<Set<XId>> mids = new SynchronousCallbackWithOneResult<Set<XId>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousCallbackWithOneResult.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		XAddress repoAddr = XX.toAddress(getRepositoryId(), null, null, null);
		for(XId modelId : mids.effect) {
			if(modelId.toString().startsWith("internal--")) {
				continue;
			}
			XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(repoAddr,
			        XCommand.FORCED, modelId);
			this.store.executeCommands(getCorrectUser(), getCorrectUserPasswordHash(),
			        new XCommand[] { removeCommand }, null);
		}
	}
	
}
