package org.xydra.core.model;

import org.junit.BeforeClass;
import org.xydra.core.TemporaryStateStore;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.test.model.AbstractChangeTest;


public class ChangeTestTemporary extends AbstractChangeTest {
	
	@BeforeClass
	public static void init() {
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
}
