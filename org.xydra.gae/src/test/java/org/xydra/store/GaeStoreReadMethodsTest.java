package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.XX;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.xgae.gaeutils.GaeTestFixer_LocalPart;
import org.xydra.xgae.gaeutils.GaeTestfixer;

public class GaeStoreReadMethodsTest extends AbstractSecureStoreReadMethodsTest {

	@BeforeClass
	public static void init() {
		// LoggerTestHelper.init();
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
		Log4jUtils.configureLog4j();
		GaeTestfixer.enable();
	}

	@Override
	protected XydraStore createStore() {
		GaeTestFixer_LocalPart.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.init();

		if (this.store == null) {
			this.store = GaePersistence.create();
		}
		return this.store;
	}

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() {
		SynchronousCallbackWithOneResult<Set<XId>> mids = new SynchronousCallbackWithOneResult<Set<XId>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousCallbackWithOneResult.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		final XAddress repoAddr = Base.toAddress(getRepositoryId(), null, null, null);
		for (final XId modelId : mids.effect) {
			if (modelId.toString().startsWith("internal--")) {
				continue;
			}
			final XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(repoAddr,
					XCommand.FORCED, modelId);
			this.store.executeCommands(getCorrectUser(), getCorrectUserPasswordHash(),
					new XCommand[] { removeCommand }, null);
		}
		mids = new SynchronousCallbackWithOneResult<Set<XId>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousCallbackWithOneResult.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		assert mids.effect.size() == 0 : mids.effect.size();
	}

	public static void main(final String[] args) {
		final GaeStoreReadMethodsTest t = new GaeStoreReadMethodsTest();
		t.setUp();
		t.setUp();
	}

}
