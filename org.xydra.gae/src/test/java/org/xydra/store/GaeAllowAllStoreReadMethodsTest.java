package org.xydra.store;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;

public class GaeAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {

	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() {
		XydraRuntime.finishRequest();
	}

	private final XId repositoryId = XX.createUniqueId();

	@Override
	protected XCommandFactory getCommandFactory() {
		return BaseRuntime.getCommandFactory();
	}

	@Override
	protected XId getRepositoryId() {
		return this.repositoryId;
	}

	@Override
	protected XydraPersistence createPersistence() {
		return new GaePersistence(this.repositoryId);
	}

}
