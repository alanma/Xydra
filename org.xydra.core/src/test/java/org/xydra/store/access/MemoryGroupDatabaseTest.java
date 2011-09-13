package org.xydra.store.access;

import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


public class MemoryGroupDatabaseTest extends AbstractGroupDatabaseTest {
	
	@Override
	protected XGroupDatabaseWithListeners getGroupDB() {
		return new MemoryGroupDatabase();
	}
	
}
