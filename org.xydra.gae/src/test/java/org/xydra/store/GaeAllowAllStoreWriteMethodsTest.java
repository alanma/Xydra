package org.xydra.store;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.xgae.gaeutils.GaeTestfixer;

public class GaeAllowAllStoreWriteMethodsTest extends AbstractAllowAllStoreWriteMethodsTest {

	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();

		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}

	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Override
	protected XydraStore createStore() {
		if (this.store == null) {
			this.store = getNewStore(new GaePersistence(XX.toId("data")));
		}

		return this.store;
	}

}
