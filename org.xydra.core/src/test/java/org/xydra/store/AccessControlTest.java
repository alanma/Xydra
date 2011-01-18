package org.xydra.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.core.XX;
import org.xydra.core.test.TestLogger;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.HashUtils;
import org.xydra.store.base.HalfWritableRepositoryOnStore;
import org.xydra.store.impl.memory.SecureMemoryStore;
import org.xydra.store.test.SynchronousTestCallback;


/**
 * A simple test that demonstrates basic access control usage.
 * 
 * @author xamde
 */
@MAXTodo
public class AccessControlTest {
	
	private XydraStore store;
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
	}
	
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
	
	static final XID user1 = XX.toId("user1");
	static final XID user2 = XX.toId("user2");
	static final XID groupA = XX.toId("groupA");
	static final XID groupB = XX.toId("groupB");
	
	@Test
	public void testStoreHasAccountModel() {
		Credentials credentials = new Credentials(XydraStoreAdmin.XYDRA_ADMIN_ID, this.store
		        .getXydraStoreAdmin().getXydraAdminPasswordHash());
		XHalfWritableRepository repo = new HalfWritableRepositoryOnStore(credentials, this.store);
		XHalfWritableModel accountModel = repo.getModel(NamingUtils.ID_ACCOUNT_MODEL);
		assertNotNull("Store is missing the account model '" + NamingUtils.ID_ACCOUNT_MODEL + "'",
		        accountModel);
	}
	
	@Test
	public void testAddActors() throws Throwable {
		XAccountDatabase accountDb = this.store.getXydraStoreAdmin().getAccountDatabase();
		// register other users
		accountDb.setPasswordHash(user1, HashUtils.getXydraPasswordHash("secret1"));
		accountDb.setPasswordHash(user2, HashUtils.getXydraPasswordHash("secret2"));
		
		assertFalse(accountDb.isValidLogin(user1, "foo"));
		assertFalse(accountDb.isValidLogin(user1, "secret1"));
		assertTrue(accountDb.isValidLogin(user1, HashUtils.getXydraPasswordHash("secret1")));
		
		accountDb.addToGroup(user1, groupA);
		assertTrue(accountDb.getMembersOf(groupA).contains(user1));
		
		// open account db again and test again
		accountDb = this.store.getXydraStoreAdmin().getAccountDatabase();
		assertTrue(accountDb.isValidLogin(user1, HashUtils.getXydraPasswordHash("secret1")));
		assertNotNull(accountDb.getPasswordHash(user1));
		assertTrue(accountDb.getMembersOf(groupA).contains(user1));
		
		SynchronousTestCallback<Boolean> callback = new SynchronousTestCallback<Boolean>();
		this.store.checkLogin(user1, HashUtils.getXydraPasswordHash("secret1"), callback);
		callback.waitOnCallback(100);
		if(!callback.getEffect()) {
			throw new RuntimeException("Could not login user1", callback.getException());
		}
	}
}
