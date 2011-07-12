package org.xydra.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XV;
import org.xydra.perf.StatsGatheringMemCacheWrapper;
import org.xydra.restless.utils.Clock;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class MemcachePerformanceTest {
	
	@BeforeClass
	public static void setup() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	@Test
	public void testWithCaching() {
		XydraRuntime.getConfigMap().put(XydraRuntime.PROP_USEMEMCACHE, "true");
		XydraRuntime.getConfigMap().put(XydraRuntime.PROP_MEMCACHESTATS, "true");
		XydraPersistence pers = XydraRuntime.getPersistence(XX.toId("test-repo"));
		assertNotNull(pers);
		assertTrue(pers instanceof GaePersistence);
		
		XWritableRepository gaeTestRepo = new WritableRepositoryOnPersistence(pers,
		        XX.toId("testactor"));
		
		Clock c = new Clock().start();
		
		XWritableModel model1 = gaeTestRepo.createModel(XX.toId("model1"));
		XWritableObject object1 = model1.createObject(XX.toId("object1"));
		XWritableField field1 = object1.createField(XX.toId("field1"));
		field1.setValue(XV.toValue("Value1"));
		
		c.stop("miniusage");
		System.out.println(c.getStats());
		
		// get stats
		IMemCache memcache = XydraRuntime.getMemcache();
		assertTrue(memcache instanceof StatsGatheringMemCacheWrapper);
		System.out.println(((StatsGatheringMemCacheWrapper)memcache).stats());
	}
	
	public static void main(String[] args) {
		setup();
		MemcachePerformanceTest test = new MemcachePerformanceTest();
		test.testWithCaching();
	}
	
}
