package org.xydra.rest.data;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.rest.RepositoryManager;
import org.xydra.rest.data.XRepositoryResource;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;



public class DataApiPreTest {
	
	@SuppressWarnings("unused")
	private LocalServiceTestHelper helper = null;
	
	@BeforeClass
	public static void init() {
		GaeTestfixer.enable();
	}
	
	XRepository repo;
	
	/**
	 * Expect a run without errors, thats all.
	 */
	@Test
	public void testTheSetupItself() {
		// initialize GAE
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// initialize XModel
		this.repo = RepositoryManager.getRepository();
		
		DemoModelUtil.addPhonebookModel(this.repo);
		
		// sanity check
		String a = "/repo/phonebook/john";
		XAddress address = X.getIDProvider().fromAddress(a);
		Key k = GaeUtils.toGaeKey(address);
		Entity e = GaeUtils.getEntity(k);
		
		assertNotNull("expect store contains entity with key " + a, e);
		
		new XRepositoryResource();
		
		RepositoryManager.getRepository().getModel(DemoModelUtil.PHONEBOOK_ID);
		
	}
	
	@After
	public void cleanup() {
		this.repo.removeModel(null, DemoModelUtil.PHONEBOOK_ID);
	}
	
}
