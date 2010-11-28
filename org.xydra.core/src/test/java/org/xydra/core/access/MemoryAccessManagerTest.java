package org.xydra.core.access;

import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.test.access.AbstractAccessManagerTest;


public class MemoryAccessManagerTest extends AbstractAccessManagerTest {
	
	@Override
	protected XAccessManagerWithListeners getAccessManager(XGroupDatabaseWithListeners groups, XAddress rA0) {
		return new MemoryAccessManager(groups);
	}
	
}
