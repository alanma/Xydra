package org.xydra.core.access;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.test.AbstractAccessManagerTest;


public class CompositeAccessManagerTestRepository extends AbstractAccessManagerTest {
	
	@Override
	protected XAccessManager getAccessManager(XGroupDatabase groups, XAddress rA0) {
		XAccessManager outer = new MemoryAccessManager(groups);
		XAccessManager inner = new MemoryAccessManager(groups);
		XAddress addr = rA0.getParent().getParent();
		assert MemoryAddress.getAddressedType(addr) == XType.XMODEL;
		return new CompositeAccessManager(addr, outer, inner);
	}
}
