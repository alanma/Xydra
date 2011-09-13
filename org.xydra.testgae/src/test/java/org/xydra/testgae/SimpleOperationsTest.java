package org.xydra.testgae;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.perf.StatsGatheringPersistenceWrapper;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class SimpleOperationsTest {
	
	private static final XID REPO_ID = XX.toId("repo");
	private static final XID MODEL1_ID = XX.toId("model1");
	private static final XID ACTOR_ID = XX.toId("actor");
	
	/**
	 * This is required to let Gae stuff run correctly in local tests.
	 */
	@Before
	public void before() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@Test
	public void testCreateModel() {
		/*
		 * turn stats gathering on for persistence. Must happen before first
		 * call to getPersistence
		 */
		XydraRuntime.getConfigMap().put(XydraRuntime.PROP_PERSISTENCESTATS, "true");
		/*
		 * turn stats gathering on for memcache. Must happen before first call
		 * to getMemcache
		 */
		XydraRuntime.getConfigMap().put(XydraRuntime.PROP_MEMCACHESTATS, "true");
		
		// get persistence
		XydraPersistence persistence = XydraRuntime.getPersistence(REPO_ID);
		assertTrue(
		        "should be a StatsGatheringPersistenceWrapper, because we set the config flag to true",
		        persistence instanceof StatsGatheringPersistenceWrapper);
		
		// trigger init
		XydraRuntime.getMemcache();
		
		// do something
		XRepositoryCommand command = X.getCommandFactory().createForcedAddModelCommand(REPO_ID,
		        MODEL1_ID);
		long l = persistence.executeCommand(ACTOR_ID, command).revision();
		assertTrue("command should be successful" + l, l >= 0);
		
		// get persistence stats, they are rather simple
		StatsGatheringPersistenceWrapper.INSTANCE.dumpStats();
		
		// get memcache stats, they are rather detailed
		System.out.println(XydraRuntime.getMemcache().stats());
	}
	
}
