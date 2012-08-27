package org.xydra.store;

import org.junit.Before;
import org.xydra.base.X;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestFixer_LocalPart;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaePersistenceSingleCommandTransactionTest extends
        AbstractSingleCommandTransactionTest {
	@Before
	public void setup() {
		GaeTestfixer.enable();
		GaeTestFixer_LocalPart.initialiseHelperAndAttachToCurrentThread();
		this.comFactory = X.getCommandFactory();
		this.persistence = new GaePersistence(this.repoId);
	}
}
