package org.xydra.store;

import org.junit.Before;
import org.xydra.base.BaseRuntime;
import org.xydra.store.impl.gae.GaePersistence;

public class GaePersistenceSingleCommandTransactionTest extends
		AbstractSingleCommandTransactionTest {
	@Before
	public void setup() {
		this.comFactory = BaseRuntime.getCommandFactory();
		this.persistence = new GaePersistence(this.repoId);
	}
}
