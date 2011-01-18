package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.test.AbstractStoreReadMethodsTest;


public class GaeStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	protected XAccountDatabase accountDb = null;
	
	@Override
	protected XydraStore getStore() {
		GaeTestfixer.enable();
		
		if(this.store == null) {
			this.store = GaePersistence.get();
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		if(this.accountDb == null) {
			// open as XydraAdmin
			this.accountDb = StoreUtils.getAccountDatabase(XydraStoreAdmin.XYDRA_ADMIN_ID, this
			        .getStore().getXydraStoreAdmin().getXydraAdminPasswordHash(), getStore());
		}
		
		XID actorId = XX.createUniqueID();
		
		if(!this.accountDb.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			this.accountDb.addToGroup(actorId, XX.toId("TestGroup"));
			this.accountDb.setPasswordHash(actorId, this.getCorrectUserPasswordHash());
		}
		assertTrue(this.accountDb.isValidLogin(actorId, this.getCorrectUserPasswordHash()));
		
		return actorId;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return "Test";
	}
	
	@Override
	protected XID getIncorrectUser() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected long getQuotaForBruteForce() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		
	}
	
	@Override
	protected XID getRepositoryId() {
		return GaePersistence.getDefaultRepositoryId();
	}
	
}
