package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.model.AbstractSynchronizeTest;
import org.xydra.server.impl.newgae.GaeTestfixer;


public class SynchronizeTestGae extends AbstractSynchronizeTest {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		XSPI.setStateStore(new GaeStateStore());
	}
	
}
