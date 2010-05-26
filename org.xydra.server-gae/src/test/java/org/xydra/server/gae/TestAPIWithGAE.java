package org.xydra.server.gae;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.AbstractTestAPI;
import org.xydra.server.gae.GaeTestfixer;



public class TestAPIWithGAE extends AbstractTestAPI {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		XSPI.setStateStore(new GaeStateStore());
	}
}
