package org.xydra.store;

import org.junit.Before;
import org.xydra.base.BaseRuntime;
import org.xydra.store.impl.memory.MemoryPersistence;


public class MemoryPersistenceTestForAtomicCommandsAndMiscellaneous extends
        AbstractPersistenceTestForAtomicCommandsAndMiscellaneous {
	@Before
	public void setup() {
		this.comFactory = BaseRuntime.getCommandFactory();
		this.persistence = new MemoryPersistence(this.repoId);
	}
}
