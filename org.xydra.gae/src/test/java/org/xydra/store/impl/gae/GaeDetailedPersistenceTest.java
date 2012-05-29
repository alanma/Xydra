package org.xydra.store.impl.gae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.X;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AbstractPersistenceTest;


public class GaeDetailedPersistenceTest extends AbstractPersistenceTest {
	
	private static final Logger log = LoggerFactory.getLogger(GaeDetailedPersistenceTest.class);
	
	@BeforeClass
	public static void beforeClazz() {
		GaeTestfixer.enable();
	}
	
	@Before
	public void before() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		super.persistence = new GaePersistence(super.repoId);
		super.persistence.clear();
		super.comFactory = X.getCommandFactory();
		
		Assert.assertTrue(log.isDebugEnabled());
	}
	
}
