package org.xydra.store;

import org.junit.Before;
import org.xydra.core.X;
import org.xydra.store.impl.gae.GaePersistence;

public class GaePersistenceSingleCommandTransactionTest extends
		AbstractSingleCommandTransactionTest {
	@Before
	public void setup() {
		this.comFactory = X.getCommandFactory();
		this.persistence = new GaePersistence(this.repoId);
	}
}
