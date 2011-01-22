package org.xydra.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.TestLogger;
import org.xydra.store.access.HashUtils;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.impl.memory.SecureMemoryStore;


/**
 * A simple test that demonstrates basic access control usage.
 * 
 * @author xamde
 */
public class AccessControlTest {
	
	static final XID groupA = XX.toId("groupA");
	
	static final XID groupB = XX.toId("groupB");
	
	static final XID user1 = XX.toId("user1");
	
	static final XID user2 = XX.toId("user2");
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
	}
	private XydraStore store;
	@Before
	public void before() {
		this.store = createStore();
	}
	/**
	 * Overwrite this method to test other implementations.
	 * 
	 * @return a {@link XydraStore} instance
	 */
	protected XydraStore createStore() {
		return new SecureMemoryStore();
	}
	
	@Test
	public void testAddActors() throws Throwable {
		XAccessControlManager acm = this.store.getXydraStoreAdmin().getAccessControlManager();
		XAuthenticationDatabase authenticationDb = acm.getAuthenticationDatabase();
		XGroupDatabase groupDb = acm.getAuthorisationManager().getGroupDatabase();
		
		// register other users
		authenticationDb.setPasswordHash(user1, HashUtils.getXydraPasswordHash("secret1"));
		authenticationDb.setPasswordHash(user2, HashUtils.getXydraPasswordHash("secret2"));
		
		assertFalse(acm.isAuthenticated(user1, "foo"));
		assertFalse(acm.isAuthenticated(user1, "secret1"));
		assertTrue(acm.isAuthenticated(user1, HashUtils.getXydraPasswordHash("secret1")));
		
		groupDb.addToGroup(user1, groupA);
		assertTrue(groupDb.getMembersOf(groupA).contains(user1));
		
		// open account db again and test again
		authenticationDb = this.store.getXydraStoreAdmin().getAccessControlManager()
		        .getAuthenticationDatabase();
		assertTrue(acm.isAuthenticated(user1, HashUtils.getXydraPasswordHash("secret1")));
		assertNotNull(authenticationDb.getPasswordHash(user1));
		assertTrue(groupDb.getMembersOf(groupA).contains(user1));
		
		SynchronousTestCallback<Boolean> callback = new SynchronousTestCallback<Boolean>();
		this.store.checkLogin(user1, HashUtils.getXydraPasswordHash("secret1"), callback);
		callback.waitOnCallback(100);
		if(!callback.getEffect()) {
			throw new RuntimeException("Could not login user1", callback.getException());
		}
	}
	
	@Test
	public void testStoreHasAccountModel() {
		// TODO test this on a store that is based on persistence
	}
}
