package org.xydra.store;

import org.junit.Before;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XID;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.impl.memory.MemoryStore;
import org.xydra.store.test.AbstractStoreReadMethodsTest;


public class MemoryStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	protected GroupModelWrapper gmw = null;
	
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
		 * FIXME this whole method propably needs to be changed, after Max tells
		 * me how to actually work with the access rights here. ~Bjoern
		 */
		if(this.gmw == null) {
			this.gmw = ((MemoryStore)this.getStore()).getGroupModelWrapper();
		}
		
		XID actorId = XX.createUniqueID();
		
		if(!this.gmw.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			/*
			 * FIXME addToGroup always fails!
			 */
			this.gmw.addToGroup(actorId, XX.toId("TestGroup"));
			this.gmw.setPasswordHash(actorId, this.getCorrectUserPasswordHash());
		}
		System.out.println("LOL "
		        + this.gmw.isValidLogin(actorId, this.getCorrectUserPasswordHash()));
		
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
		if(this.setUpDone) {
			return;
		}
		
		super.setUp();
		
	}
	
}
