package org.xydra.store;

import org.junit.Before;
import org.xydra.core.X;
import org.xydra.store.impl.memory.MemoryPersistence;


public class MemoryPersistenceTestForTransactions extends AbstractPersistenceTestForTransactions {
	@Before
	public void setup() {
		this.comFactory = X.getCommandFactory();
		this.persistence = new MemoryPersistence(this.repoId);
	}
}
