package org.xydra.server.test;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.server.RepositoryManager;


/**
 * Simple test to see if anything works.
 * 
 */
public abstract class PreTest {
	
	XRepository repo;
	
	/**
	 * Expect a run without errors, thats all.
	 */
	@Test
	public void testTheSetupItself() {
		
		// initialize XModel
		this.repo = RepositoryManager.getRepository();
		
		DemoModelUtil.addPhonebookModel(this.repo);
		
		assertNotNull(RepositoryManager.getRepository().getModel(DemoModelUtil.PHONEBOOK_ID));
		
	}
	
	@After
	public void cleanup() {
		this.repo.removeModel(null, DemoModelUtil.PHONEBOOK_ID);
	}
	
}
