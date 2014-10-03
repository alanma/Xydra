package org.xydra.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.utils.NanoClock;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;

public class MemcachePerformanceTest {

	@BeforeClass
	public static void setup() {
	}

	@Test
	public void testWithCaching() {
		XydraRuntime.getConfigMap().put(XydraRuntime.PROP_USEMEMCACHE, "true");
		XydraRuntime.getConfigMap().put(XydraRuntime.PROP_MEMCACHESTATS, "true");
		XydraRuntime.forceReInitialisation();
		XydraPersistence pers = XydraRuntime.getPersistence(XX.toId("test-repo"));
		assertNotNull(pers);
		assertTrue("pers is " + pers.getClass().getCanonicalName(), pers instanceof GaePersistence);

		XWritableRepository gaeTestRepo = new WritableRepositoryOnPersistence(pers,
				XX.toId("testactor"));

		NanoClock c = new NanoClock().start();

		XWritableModel model1 = gaeTestRepo.createModel(XX.toId("model1"));
		XWritableObject object1 = model1.createObject(XX.toId("object1"));
		XWritableField field1 = object1.createField(XX.toId("field1"));
		field1.setValue(XV.toValue("Value1"));

		c.stop("miniusage");
		System.out.println(c.getStats());

		// get stats
		// TODO enable stats with a working api
		// IMemCache memcache = XydraRuntime.getMemcache();
		// assertTrue("memcache is " + memcache.getClass().getCanonicalName(),
		// memcache instanceof StatsGatheringMemCacheWrapper);
		// System.out.println(((StatsGatheringMemCacheWrapper)memcache).stats());
	}

	public static void main(String[] args) {
		setup();
		MemcachePerformanceTest test = new MemcachePerformanceTest();
		test.testWithCaching();
	}

}
