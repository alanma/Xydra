package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XID;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.access.XPasswordDatabase;
import org.xydra.store.base.HashUtils;
import org.xydra.store.impl.memory.MemoryStore;
import org.xydra.store.test.AbstractStoreReadMethodsTest;


public class MemoryStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	protected GroupModelWrapper gmw = null;
	protected String correctPass = "Test";
	
	@Override
	protected XydraStore getStore() {
		if(this.store != null) {
			return this.store;
		}
		
		this.store = new MemoryStore();
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		/*
		 * FIXME this whole method probably needs to be changed, after Max tells
		 * me how to actually work with the access rights here. ~Bjoern
		 */
		if(this.gmw == null) {
			this.gmw = ((MemoryStore)this.getStore()).getGroupModelWrapper();
		}
		XPasswordDatabase pwdbase = this.gmw;
		
		XID actorId = XX.createUniqueID();
		
		if(!this.gmw.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			pwdbase.setPasswordHash(actorId, HashUtils.getXydraPasswordHash(this.correctPass));
		}
		assertTrue(this.gmw.isValidLogin(actorId, this.getCorrectUserPasswordHash()));
		
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
		if(this.gmw == null) {
			this.gmw = ((MemoryStore)this.getStore()).getGroupModelWrapper();
		}
		XPasswordDatabase pwdbase = this.gmw;
		
		XID actorId = XX.createUniqueID();
		
		if(!this.gmw.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			pwdbase.setPasswordHash(actorId, HashUtils.getXydraPasswordHash("correct"));
		}
		assertTrue(this.gmw.isValidLogin(actorId, HashUtils.getXydraPasswordHash("correct")));
		
		return actorId;
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash("incorrect");
	}
	
	@Override
	protected long getQuotaForBruteForce() {
		return MemoryStore.MAX_FAILED_LOGIN_ATTEMPTS;
	}
	
	@Override
	protected XID getRepositoryId() {
		return XX.toId("data");
		// repositoryId as set in the standard constructor of {@link
		// MemoryStore}
	}
	
}
