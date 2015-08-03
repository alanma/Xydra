package org.xydra.store.impl.delegate;

import org.xydra.base.XId;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraStore;
import org.xydra.store.access.impl.delegate.AccessControlManagerOnPersistence;


/**
 * An implementation of {@link XydraStore} with authorisation and access
 * control. Persistence is delegated.
 *
 * Every component that has a reference to this {@link DelegatingSecureStore}
 * instance has complete access to change everything.
 *
 * @author xamde
 */

public class DelegatingSecureStore extends DelegatingStore implements XydraStore {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DelegatingSecureStore.class);

	public DelegatingSecureStore(final XydraPersistence persistence, final XId executingActorId) {
		super(persistence, new AccessControlManagerOnPersistence(persistence, executingActorId));
	}

}
