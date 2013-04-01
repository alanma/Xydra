package org.xydra.store.impl.memory;

import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;


/**
 * An in-memory implementation of {@link XydraStore} with authorisation and
 * access control.
 * 
 * @author xamde
 */
public class SecureMemoryStore extends DelegatingSecureStore {
	
	public static final XId DEFAULT_REPOSITORY_ID = XX.toId("data");
	
	public SecureMemoryStore() {
		super(new MemoryPersistence(DEFAULT_REPOSITORY_ID), XydraStoreAdmin.XYDRA_ADMIN_ID);
	}
	
}
