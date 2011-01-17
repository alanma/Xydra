package org.xydra.store.impl.memory;

import org.xydra.core.model.XID;
import org.xydra.store.MAXDone;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.DelegatingStore;


/**
 * A {@link XydraStore} implementation with in-memory persistence. Every actorId
 * is authorised and has access rights to perform any operation.
 * 
 * @author xamde
 */
@MAXDone
public class AllowAllMemoryStore extends DelegatingStore {
	
	public AllowAllMemoryStore(XID repositoryId) {
		super(new MemoryPersistence(repositoryId), new AllowAllAuthorisationArm());
	}
	
}
