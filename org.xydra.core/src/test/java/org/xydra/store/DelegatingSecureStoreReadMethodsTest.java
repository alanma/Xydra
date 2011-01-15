package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XID;
import org.xydra.store.access.XPasswordDatabase;
import org.xydra.store.base.HashUtils;
import org.xydra.store.impl.delegating.DelegatingSecureStore;
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
	
	protected XPasswordDatabase passwordDb = null;
	protected String correctPass = "Test";
	
	@Override
	protected XydraStore getStore() {
		if(this.store != null) {
			return this.store;
		}
		
		this.store = new DelegatingSecureStore();
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		if(this.passwordDb == null) {
			this.passwordDb = ((DelegatingSecureStore)this.getStore()).getPasswordDatabase();
		}
		XPasswordDatabase pwdbase = this.passwordDb;
		
		XID actorId = XX.createUniqueID();
		
		if(!this.passwordDb.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			pwdbase.setPasswordHash(actorId, HashUtils.getXydraPasswordHash(this.correctPass));
		}
		assertTrue(this.passwordDb.isValidLogin(actorId, this.getCorrectUserPasswordHash()));
		
		return actorId;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash(this.correctPass);
	}
	
	@Override
	protected XID getIncorrectUser() {
		/*
		 * FIXME this whole method probably needs to be changed, after Max tells
		 * me how to actually work with the access rights here. ~Bjoern
		 */
		if(this.passwordDb == null) {
			this.passwordDb = ((DelegatingSecureStore)this.getStore()).getPasswordDatabase();
		}
		XPasswordDatabase pwdbase = this.passwordDb;
		
		XID actorId = XX.createUniqueID();
		
		if(!this.passwordDb.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			pwdbase.setPasswordHash(actorId, HashUtils.getXydraPasswordHash("correct"));
		}
		assertTrue(this.passwordDb.isValidLogin(actorId, HashUtils.getXydraPasswordHash("correct")));
		
		return actorId;
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash("incorrect");
	}
	
	@Override
	protected long getQuotaForBruteForce() {
		return DelegatingSecureStore.MAX_FAILED_LOGIN_ATTEMPTS;
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
