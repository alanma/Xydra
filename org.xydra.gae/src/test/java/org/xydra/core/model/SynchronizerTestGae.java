package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class SynchronizerTestGae extends AbstractSynchronizerTest {
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
		GaeTestfixer.enable();
		XSPI.setStateStore(new TemporaryStateStore());
		actorId = XX.toId("tester");
		passwordHash = "top secret";
		store = new DelegatingAllowAllStore(new GaePersistence(XX.toId("repo")));
	}
	
}
