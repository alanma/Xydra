package org.xydra.store;

import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.X;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeAllowAllStoreWriteMethodsTest extends AbstractAllowAllStoreWriteMethodsTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Before
	public void setUp() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		super.setUp();
	}
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = getNewStore(new GaePersistence(XX.toId("data")));
		}
		
		return this.store;
	}
	
}
