package org.xydra.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableRepository;
import org.xydra.core.test.TestLogger;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.HashUtils;
import org.xydra.store.base.WritableRepository;
import org.xydra.store.impl.memory.SecureMemoryStore;


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
		XWritableRepository repo = new WritableRepository(credentials, this.store);
		XWritableModel accountModel = repo.getModel(NamingUtils.ID_ACCOUNT_MODEL);
		assertNotNull("Store is missing the account model '" + NamingUtils.ID_ACCOUNT_MODEL + "'",
		        accountModel);
	}
	
	@Test
	public void testAddAndRemoveOneActor() {
		// get admin account
		XID actorId = XydraStoreAdmin.XYDRA_ADMIN_ID;
		String passwordHash = this.store.getXydraStoreAdmin().getXydraAdminPasswordHash();
		assertNotNull(passwordHash);
		// open accountDb via admin account
		XAccountDatabase accountDb = StoreUtils.getAccountDatabase(actorId, passwordHash,
		        this.store);
		// register other users
		accountDb.setPasswordHash(user1, HashUtils.getXydraPasswordHash("secret1"));
		accountDb.setPasswordHash(user2, HashUtils.getXydraPasswordHash("secret2"));
		
		assertFalse(accountDb.isValidLogin(user1, "foo"));
		assertFalse(accountDb.isValidLogin(user1, "secret1"));
		assertTrue(accountDb.isValidLogin(user1, HashUtils.getXydraPasswordHash("secret1")));
		
		accountDb.addToGroup(user1, groupA);
		assertTrue(accountDb.getMembersOf(groupA).contains(user1));
	}
}
