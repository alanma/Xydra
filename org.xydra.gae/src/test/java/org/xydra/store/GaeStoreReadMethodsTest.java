package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.BeforeClass;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeStoreReadMethodsTest extends AbstractSecureStoreReadMethodsTest {
	
	@BeforeClass
	public static void init() {
		// LoggerTestHelper.init();
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
		Log4jUtils.configureLog4j();
	}
	
	@Override
	protected XydraStore getStore() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.init();
		
		if(this.store == null) {
			this.store = GaePersistence.get();
		}
		return this.store;
	}
	
	@After
	public void after() {
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
		mids = new SynchronousCallbackWithOneResult<Set<XId>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousCallbackWithOneResult.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		assert mids.effect.size() == 0 : mids.effect.size();
	}
	
	public static void main(String[] args) {
		GaeStoreReadMethodsTest t = new GaeStoreReadMethodsTest();
		t.before();
		t.before();
	}
	
}
