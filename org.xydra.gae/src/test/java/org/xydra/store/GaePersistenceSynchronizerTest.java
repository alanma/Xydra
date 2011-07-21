package org.xydra.store;

import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.AbstractPersistenceSynchronizerTest;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaePersistenceSynchronizerTest extends AbstractPersistenceSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		persistence = new GaePersistence(XX.toId("repo"));
		AbstractPersistenceSynchronizerTest.init();
	}
	
}
