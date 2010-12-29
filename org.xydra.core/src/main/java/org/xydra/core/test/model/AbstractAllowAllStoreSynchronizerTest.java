package org.xydra.core.test.model;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.store.impl.memory.AllowAllStore;
import org.xydra.store.impl.memory.XydraNoAccessRightsNoBatchNoAsyncStore;


abstract public class AbstractAllowAllStoreSynchronizerTest extends AbstractSynchronizerTest {
	
	protected static XydraNoAccessRightsNoBatchNoAsyncStore simpleStore;
	
	@BeforeClass
	public static void init() {
		assertNotNull(simpleStore);
		actorId = XX.toId("tester");
		passwordHash = "top secret";
		repoAddr = XX.toAddress(simpleStore.getRepositoryId(), null, null, null);
		store = new AllowAllStore(simpleStore);
	}
	
}
