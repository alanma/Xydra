package org.xydra.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.test.TestLogger;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.XPasswordDatabase;
import org.xydra.store.base.HashUtils;
import org.xydra.store.impl.delegating.DelegatingSecureStore;


/**
 * A simple test that demonstrates basic acess control usage.
 * 
 * @author xamde
 */
public class AccessControlTest {
	
	private DelegatingSecureStore store;
	
	@BeforeClass
	public static void init() {
		TestLogger.init();
	}
	
	@Before
	public void before() {
		this.store = new DelegatingSecureStore();
	}
	
	static final XID user1 = XX.toId("user1");
	static final XID user2 = XX.toId("user2");
	static final XID groupA = XX.toId("groupA");
	static final XID groupB = XX.toId("groupB");
	
	@Test
	public void testAddAndRemoveOneActor() {
		// register users
		XPasswordDatabase passwordDb = this.store.getPasswordDatabase();
		passwordDb.setPasswordHash(user1, HashUtils.getXydraPasswordHash("secret1"));
		passwordDb.setPasswordHash(user2, HashUtils.getXydraPasswordHash("secret2"));
		
		assertFalse(passwordDb.isValidLogin(user1, "foo"));
		assertFalse(passwordDb.isValidLogin(user1, "secret1"));
		assertTrue(passwordDb.isValidLogin(user1, HashUtils.getXydraPasswordHash("secret1")));
		
		XGroupDatabase groupDb = this.store.getGroupDatabase();
		groupDb.addToGroup(user1, groupA);
		assertTrue(groupDb.getMembersOf(groupA).contains(user1));
	}
}
