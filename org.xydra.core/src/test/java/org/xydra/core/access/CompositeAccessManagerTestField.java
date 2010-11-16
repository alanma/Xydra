package org.xydra.core.access;

import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;
import org.xydra.core.test.access.AbstractAccessManagerTest;


public class CompositeAccessManagerTestField extends AbstractAccessManagerTest {
	
	@Override
	protected XAccessManager getAccessManager(XGroupDatabaseWithListeners groups, XAddress rA0) {
		XAccessManager outer = new MemoryAccessManager(groups);
		XAccessManager inner = new MemoryAccessManager(groups);
		assert rA0.getAddressedType() == XType.XFIELD;
		return new CompositeAccessManager(rA0, outer, inner);
	}
	
}
