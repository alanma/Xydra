package org.xydra.store.access;

import org.xydra.base.XAddress;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;


public class MemoryAuthorisationManagerTest extends AbstractAuthorisationManagerTest {
	
	@Override
	protected XAuthorisationManager getAccessManager(XGroupDatabaseWithListeners groups,
	        XAddress rA0) {
		return new MemoryAuthorisationManager(groups);
	}
	
}
