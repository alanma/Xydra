package org.xydra.core.access;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.test.access.AbstractAccessManagerTest;


public class CompositeAccessManagerTestModel extends AbstractAccessManagerTest {
	
	@Override
	protected XAccessManager getAccessManager(XGroupDatabaseWithListeners groups,
	        XAddress rA0) {
		XAccessManager outer = new MemoryAccessManager(groups);
		XAccessManager inner = new MemoryAccessManager(groups);
		XAddress addr = rA0.getParent().getParent().getParent();
		assert addr.getAddressedType() == XType.XREPOSITORY;
		return new CompositeAccessManager(addr, outer, inner);
	}
}
