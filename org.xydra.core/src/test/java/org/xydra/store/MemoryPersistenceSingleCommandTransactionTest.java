package org.xydra.store;

import org.junit.Before;
import org.xydra.base.X;
import org.xydra.store.impl.memory.MemoryPersistence;


public class MemoryPersistenceSingleCommandTransactionTest extends
        AbstractSingleCommandTransactionTest {
	@Before
	public void setup() {
		this.comFactory = X.getCommandFactory();
		this.persistence = new MemoryPersistence(this.repoId);
	}
}
