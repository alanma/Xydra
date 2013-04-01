package org.xydra.store;

import org.junit.After;
import org.junit.BeforeClass;
import org.xydra.base.XId;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	@After
	public void after() {
		XydraRuntime.finishRequest();
	}
	
	private XId repositoryId;
	
	@Override
	protected XydraStore getStore() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		this.repositoryId = XX.createUniqueId();
		if(this.store == null) {
			this.store = getNewStore(new GaePersistence(this.repositoryId));
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XId getRepositoryId() {
		return this.repositoryId;
	}
	
}
