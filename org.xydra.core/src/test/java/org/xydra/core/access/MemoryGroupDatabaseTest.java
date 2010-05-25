package org.xydra.core.access;

import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.test.AbstractGroupDatabaseTest;


public class MemoryGroupDatabaseTest extends AbstractGroupDatabaseTest {
	
	@Override
	protected XGroupDatabase getGroupDB() {
		return new MemoryGroupDatabase();
	}
	
}
