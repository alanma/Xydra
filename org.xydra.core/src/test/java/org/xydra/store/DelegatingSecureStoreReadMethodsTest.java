package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.access.HashUtils;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.impl.memory.SecureMemoryStore;


/**
 * TODO testing for quote exceptions is very slow, because the implementation
 * lets the caller waiting for a long time to throttle the hacking attempt - all
 * tests testing the quota stuff should go into another class so that this class
 * can also be used for performance tests.
 * 
 * @author xamde
 * 
 */
public class DelegatingSecureStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	private XAccessControlManager acm;
	protected XAuthenticationDatabase authenticationDb = null;
	protected String correctPass = "Test";
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		if(this.authenticationDb == null) {
			this.authenticationDb = this.store.getXydraStoreAdmin().getAccessControlManager()
			        .getAuthenticationDatabase();
		}
		if(this.acm == null) {
			this.acm = this.store.getXydraStoreAdmin().getAccessControlManager();
		}
		
		// easier in the debugger
		XID actorId = XX.toId("SecureDirk");
		
		if(!this.acm.isAuthenticated(actorId, this.getCorrectUserPasswordHash())) {
			this.authenticationDb.setPasswordHash(actorId,
			        HashUtils.getXydraPasswordHash(this.correctPass));
		}
		assertTrue(this.acm.isAuthenticated(actorId, this.getCorrectUserPasswordHash()));
		
		return actorId;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash(this.correctPass);
	}
	
	@Override
	protected XID getIncorrectUser() {
		/*
		 * By definition of createUniqueID this ID is unknown an is therefore
		 * not registered in the accountDb
		 */
		return XX.createUniqueID();
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash("incorrect");
	}
	
	@Override
	protected long getQuotaForBruteForce() {
		return XydraStore.MAX_FAILED_LOGIN_ATTEMPTS;
	}
	
	@Override
	protected XID getRepositoryId() {
		return XX.toId("data");
		// repositoryId as set in the standard constructor of {@link
		// MemoryStore}
	}
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = new SecureMemoryStore();
		}
		return this.store;
	}
	
	/*
	 * TODO Tests for QuoataException are painfully slow - can we do something
	 * about that? (Propably not, since it's a "problem" of the implementation)
	 */

}
