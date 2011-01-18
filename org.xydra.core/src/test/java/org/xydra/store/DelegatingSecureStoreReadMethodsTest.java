package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.base.HashUtils;
import org.xydra.store.impl.delegate.AuthorisationArm;
import org.xydra.store.impl.memory.SecureMemoryStore;
import org.xydra.store.test.AbstractStoreReadMethodsTest;


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
	
	protected XAccountDatabase accountDb = null;
	protected String correctPass = "Test";
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = new SecureMemoryStore();
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
			this.accountDb = this.store.getXydraStoreAdmin().getAccountDatabase();
		}
		
		XID actorId = XX.createUniqueID();
		
		if(!this.accountDb.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			this.accountDb.setPasswordHash(actorId,
			        HashUtils.getXydraPasswordHash(this.correctPass));
		}
		assertTrue(this.accountDb.isValidLogin(actorId, this.getCorrectUserPasswordHash()));
		
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
		return AuthorisationArm.MAX_FAILED_LOGIN_ATTEMPTS;
	}
	
	@Override
	protected XID getRepositoryId() {
		return XX.toId("data");
		// repositoryId as set in the standard constructor of {@link
		// MemoryStore}
	}
	
	/*
	 * TODO Tests for QuoataException are painfully slow - can we do something
	 * about that? (Propably not, since it's a "problem" of the implementation)
	 */

}
