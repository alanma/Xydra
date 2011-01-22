package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
	
	private XID repositoryID = XX.createUniqueID();
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			GaeTestfixer.enable();
			this.store = getNewStore(new GaePersistence(this.repositoryID));
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	/*
	 * Tests for getRepositoryId
	 */
	@Test
	public void testGetRepositoryId() {
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
		return this.repositoryID;
	}
	
}
