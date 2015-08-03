package org.xydra.store.access;

import org.xydra.base.XAddress;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;


public class MemoryAuthorisationManagerTest extends AbstractAuthorisationManagerTest {

	@Override
	protected XAuthorisationManager getAccessManager(final XGroupDatabaseWithListeners groups,
	        final XAddress rA0) {
		return new MemoryAuthorisationManager(groups);
	}

}
