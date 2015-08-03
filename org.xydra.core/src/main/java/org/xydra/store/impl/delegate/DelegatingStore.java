package org.xydra.store.impl.delegate;

import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraStore;
import org.xydra.store.access.XAccessControlManager;


/**
 * Implement {@link XydraStore} by delegating through a chain of delegates,
 * ultimately persisting state in a {@link XydraPersistence} and managing
 * authorisation and access rights via {@link XAccessControlManager}.
 *
 * @author xamde
 */

public class DelegatingStore extends DelegateToSingleOperationStore implements XydraStore {

	public DelegatingStore(final XydraPersistence persistence, final XAccessControlManager arm) {
		super(new DelegateToBlockingStore(new DelegateToPersistenceAndAcm(persistence, arm)));
	}

}
