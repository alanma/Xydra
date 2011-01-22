package org.xydra.core.access;

import org.xydra.base.XAddress;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;


public class MemoryAccessManagerTest extends AbstractAccessManagerTest {
	
	@Override
	protected XAuthorisationManager getAccessManager(XGroupDatabaseWithListeners groups,
	        XAddress rA0) {
		return new MemoryAuthorisationManager(groups);
	}
	
}
