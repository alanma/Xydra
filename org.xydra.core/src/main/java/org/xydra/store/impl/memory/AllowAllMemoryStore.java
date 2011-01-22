package org.xydra.store.impl.memory;

import org.xydra.base.XID;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.DelegatingStore;


/**
 * A {@link XydraStore} implementation with in-memory persistence. Every actorId
 * is authorised and has access rights to perform any operation.
 * 
 * @author xamde
 */
public class AllowAllMemoryStore extends DelegatingStore {
	
	public AllowAllMemoryStore(XID repositoryId) {
		super(new MemoryPersistence(repositoryId), new AllowAllAccessControlManager());
	}
	
}
