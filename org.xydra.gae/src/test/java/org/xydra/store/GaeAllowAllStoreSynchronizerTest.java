package org.xydra.store;

import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.AbstractAllowAllStoreSynchronizerTest;
import org.xydra.store.impl.gae.GaePersistence;


public class GaeAllowAllStoreSynchronizerTest extends AbstractAllowAllStoreSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		simpleStore = new GaePersistence(XX.toId("repo"));
		AbstractAllowAllStoreSynchronizerTest.init();
	}
	
}
