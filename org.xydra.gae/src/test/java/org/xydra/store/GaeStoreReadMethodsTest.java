package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.BeforeClass;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeStoreReadMethodsTest extends AbstractSecureStoreReadMethodsTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	@Override
	protected XydraStore getStore() {
		GaeTestfixer.enable();
		
		if(this.store == null) {
			this.store = GaePersistence.get();
		}
		
		return this.store;
	}
	
	@Override
	protected XID getRepositoryId() {
		return GaePersistence.getDefaultRepositoryId();
	}
	
	@After
	public void tearDown() {
		SynchronousTestCallback<Set<XID>> mids = new SynchronousTestCallback<Set<XID>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousTestCallback.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		XAddress repoAddr = XX.toAddress(getRepositoryId(), null, null, null);
		for(XID modelId : mids.effect) {
			XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(repoAddr,
			        XCommand.FORCED, modelId);
			this.store.executeCommands(getCorrectUser(), getCorrectUserPasswordHash(),
			        new XCommand[] { removeCommand }, null);
		}
	}
}
