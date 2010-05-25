package org.xydra.core.access;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.test.AbstractAccessManagerTest;


public class MemoryAccessManagerTest extends AbstractAccessManagerTest {
	
	@Override
	protected XAccessManager getAccessManager(XGroupDatabase groups, XAddress rA0) {
		return new MemoryAccessManager(groups);
	}
	
}
