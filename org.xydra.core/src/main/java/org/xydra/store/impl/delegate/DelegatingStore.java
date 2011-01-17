package org.xydra.store.impl.delegate;

import org.xydra.store.MAXDone;
import org.xydra.store.XydraStore;


/**
 * Implement {@link XydraStore} by delegating through a chain of delegates,
 * ultimately persisting state in a {@link XydraPersistence} and managing
 * authorisation and access rights via {@link XAuthorisationArm}.
 * 
 * @author xamde
 */
@MAXDone
public class DelegatingStore extends DelegateToSingleOperationStore implements XydraStore {
	
	public DelegatingStore(XydraPersistence persistence, XAuthorisationArm arm) {
		super(new DelegateToBlockingStore(new DelegateToPersistenceAndArm(persistence, arm)));
	}
	
}
