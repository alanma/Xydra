package org.xydra.store.impl.memory;

import org.xydra.base.XId;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;


/**
 * A {@link XydraStore} implementation with in-memory persistence. Every actorId
 * is authorised and has access rights to perform any operation.
 *
 * @author xamde
 */
public class AllowAllMemoryStore extends DelegatingAllowAllStore {

	public AllowAllMemoryStore(final XId repositoryId) {
		super(new MemoryPersistence(repositoryId));
	}

}
