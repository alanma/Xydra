package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.access.HashUtils;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	private static final XID DIRK = XX.toId("user-dirk");
	private static final String DIRKS_PASS = "hello";
	
	@Before
	public void before() {
		XAccessControlManager acm = getStore().getXydraStoreAdmin().getAccessControlManager();
		acm.getAuthenticationDatabase().setPasswordHash(DIRK,
		        HashUtils.getXydraPasswordHash(DIRKS_PASS));
		assertTrue(acm.isAuthenticated(getCorrectUser(), getCorrectUserPasswordHash()));
	}
	
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
		return DIRK;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash(DIRKS_PASS);
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
