package org.xydra.store;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class PerformanceTest {
	
	@BeforeClass
	public static void setup() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	private static void runTest(boolean localCaching) {
		long start = System.currentTimeMillis();
		XydraPersistence gaePersistence = new GaePersistence(XX.toId("memrepo"));
		XWritableRepository gaeTestRepo = new WritableRepositoryOnPersistence(
		        localCaching ? new LocalVmCachingPersistence(gaePersistence) : gaePersistence,
		        XX.toId("testactor"));
		
		// set up demo data
		MemoryRepository demoRepo = new MemoryRepository(XX.toId("demoActor"), null,
		        XX.toId("demoRepo"));
		DemoModelUtil.addPhonebookModel(demoRepo);
		
		// copy: simulate writes
		XCopyUtils.copyData(demoRepo, gaeTestRepo);
		// copy: simulate reads
		XCopyUtils.copyData(gaeTestRepo, demoRepo);
		
		long stop = System.currentTimeMillis();
		System.out.println("Took " + (stop - start) + " ms with localCaching?" + localCaching);
	}
	
	@Test
	public void testWithCaching() {
		runTest(true);
	}
	
	@Test
	public void testWithoutCaching() {
		runTest(false);
	}
	
	public static void main(String[] args) {
		setup();
		runTest(true);
		runTest(false);
	}
	
}
