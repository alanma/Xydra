package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.test.TestLogger;
import org.xydra.core.test.model.AbstractSynchronizerTest;
import org.xydra.store.impl.gae.GaeXydraStore;
import org.xydra.store.impl.memory.AllowAllStore;


public class SynchronizerTestGae extends AbstractSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		XSPI.setStateStore(new TemporaryStateStore());
		actorId = XX.toId("tester");
		passwordHash = "top secret";
		store = new AllowAllStore(new GaeXydraStore(XX.toId("repo")));
	}
	
}
