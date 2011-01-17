package org.xydra.store.impl.memory;

import org.xydra.core.XX;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.DelegatingSecureStore;


/**
 * An in-memory implementation of {@link XydraStore} with authorisation and
 * access control.
 * 
 * @author xamde
 */
public class SecureMemoryStore extends DelegatingSecureStore {
	
	public SecureMemoryStore() {
		super(new MemoryPersistence(XX.toId("data")));
	}
	
}
