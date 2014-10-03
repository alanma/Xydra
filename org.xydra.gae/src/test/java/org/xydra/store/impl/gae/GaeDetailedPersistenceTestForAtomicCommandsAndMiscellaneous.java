package org.xydra.store.impl.gae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.X;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.AbstractPersistenceTestForAtomicCommandsAndMiscellaneous;
import org.xydra.store.XydraRuntime;

/*
 * TODO This test sometimes requires a lot of heap memory to run, why?
 */
public class GaeDetailedPersistenceTestForAtomicCommandsAndMiscellaneous extends
		AbstractPersistenceTestForAtomicCommandsAndMiscellaneous {

	private static final Logger log = LoggerFactory
			.getLogger(GaeDetailedPersistenceTestForAtomicCommandsAndMiscellaneous.class);

	@BeforeClass
	public static void beforeClazz() {
		XydraRuntime.hardReset();
	}

	@Before
	public void setUp() {
		XydraRuntime.forceReInitialisation();

		super.persistence = new GaePersistence(super.repoId);
		super.persistence.clear();
		super.comFactory = X.getCommandFactory();

		Assert.assertTrue(log.isDebugEnabled());
	}

}
