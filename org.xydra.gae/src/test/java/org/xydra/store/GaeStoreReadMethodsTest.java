package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XID;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.XPasswordDatabase;
import org.xydra.store.impl.delegating.DelegatingSecureStore;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeXydraStore;
import org.xydra.store.test.AbstractStoreReadMethodsTest;


public class GaeStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	protected XPasswordDatabase passwordDb = null;
	protected XGroupDatabase groupDb = null;
	
	@Override
	protected XydraStore getStore() {
		if(this.store != null) {
			GaeTestfixer.enable();
			return this.store;
		}
		
		this.store = GaeXydraStore.get();
		
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
		if(this.groupDb == null) {
			this.groupDb = ((DelegatingSecureStore)this.getStore()).getGroupDatabase();
		}
		
		XID actorId = XX.createUniqueID();
		
		if(!this.passwordDb.isValidLogin(actorId, this.getCorrectUserPasswordHash())) {
			this.groupDb.addToGroup(actorId, XX.toId("TestGroup"));
			this.passwordDb.setPasswordHash(actorId, this.getCorrectUserPasswordHash());
		}
		assertTrue(this.passwordDb.isValidLogin(actorId, this.getCorrectUserPasswordHash()));
		
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
	
	@Override
	protected XID getRepositoryId() {
		return GaeXydraStore.getDefaultRepositoryId();
	}
	
}
