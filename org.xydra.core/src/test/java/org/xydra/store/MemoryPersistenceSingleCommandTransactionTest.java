package org.xydra.store;

import org.junit.Before;
import org.xydra.base.BaseRuntime;
import org.xydra.core.X;
import org.xydra.store.impl.memory.MemoryPersistence;


public class MemoryPersistenceSingleCommandTransactionTest extends
        AbstractSingleCommandTransactionTest {
	@Before
	public void setup() {
		this.comFactory = BaseRuntime.getCommandFactory();
		this.persistence = new MemoryPersistence(this.repoId);
	}
}
