package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.model.AbstractChangeTest;
import org.xydra.store.impl.gae.GaeTestfixer;


public class ChangeTestGae extends AbstractChangeTest {
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
		XSPI.setStateStore(new GaeStateStore());
	}
	
}
