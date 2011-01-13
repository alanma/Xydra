package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XID;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeXydraStore;
import org.xydra.store.impl.memory.SynchronousNoAccessRightsStore;
import org.xydra.store.test.AbstractAllowAllStoreReadMethodsTest;
import org.xydra.store.test.SynchronousTestCallback;


public class GaeAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
	
	private XID repositoryID = XX.createUniqueID();
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			GaeTestfixer.enable();
			this.store = getNewStore(new SynchronousNoAccessRightsStore(new GaeXydraStore(
			        this.repositoryID)));
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	/*
	 * Tests for getRepositoryID
	 */
	@Test
	public void testGetRepositoryID() {
		XID correctUser = this.getCorrectUser();
		String correctUserPass = this.getCorrectUserPasswordHash();
		
		SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
		
		this.store.getRepositoryId(correctUser, correctUserPass, callback);
		
		assertTrue(waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertEquals(callback.getEffect(), this.repositoryID);
		assertNull(callback.getException());
	}
	
	@Override
	protected XID getRepositoryId() {
		// FIXME !!!
		return null;
	}
	
}
