package org.xydra.core.access;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;


public class CompositeAccessManagerTestModel extends AbstractAccessManagerTest {
	
	@Override
	protected XAuthorisationManager getAccessManager(XGroupDatabaseWithListeners groups,
	        XAddress rA0) {
		XAuthorisationManager outer = new MemoryAuthorisationManager(groups);
		XAuthorisationManager inner = new MemoryAuthorisationManager(groups);
		XAddress addr = rA0.getParent().getParent().getParent();
		assert addr.getAddressedType() == XType.XREPOSITORY;
		return new CompositeAccessManager(addr, outer, inner);
	}
}
