package org.xydra.core.access;

import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;
import org.xydra.core.test.access.AbstractAccessManagerTest;


public class CompositeAccessManagerTestObject extends AbstractAccessManagerTest {
	
	@Override
	protected XAccessManager getAccessManager(XGroupDatabase groups, XAddress rA0) {
		XAccessManager outer = new MemoryAccessManager(groups);
		XAccessManager inner = new MemoryAccessManager(groups);
		XAddress addr = rA0.getParent();
		assert addr.getAddressedType() == XType.XOBJECT;
		return new CompositeAccessManager(addr, outer, inner);
	}
	
}
