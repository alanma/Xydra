package org.xydra.server.test;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.server.IXydraServer;


/**
 * Simple test to see if anything works.
 * 
 */
public abstract class PreTest {
	
	XRepository repo;
	
	// to be initialized by subclasses
	protected static IXydraServer xydraServer;
	
	/**
	 * Expect a run without errors, thats all.
	 */
	@Test
	public void testTheSetupItself() {
		
		assertNotNull(xydraServer);
		
		// initialize XModel
		this.repo = xydraServer.getRepository();
		assertNotNull(this.repo);
		
		DemoModelUtil.addPhonebookModel(this.repo);
		
		assertNotNull(xydraServer.getRepository().getModel(DemoModelUtil.PHONEBOOK_ID));
		
	}
	
	@After
	public void tearDown() {
		this.repo.removeModel(null, DemoModelUtil.PHONEBOOK_ID);
	}
	
}
