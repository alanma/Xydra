package org.xydra.core.access;

import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


public class MemoryGroupDatabaseTest extends AbstractGroupDatabaseTest {
	
	@Override
	protected XGroupDatabaseWithListeners getGroupDB() {
		return new MemoryGroupDatabase();
	}
	
}
