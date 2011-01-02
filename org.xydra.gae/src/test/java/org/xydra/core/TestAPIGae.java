package org.xydra.core;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.AbstractTestAPI;
import org.xydra.store.impl.gae.GaeTestfixer;


public class TestAPIGae extends AbstractTestAPI {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		XSPI.setStateStore(new GaeStateStore());
	}
	
}
