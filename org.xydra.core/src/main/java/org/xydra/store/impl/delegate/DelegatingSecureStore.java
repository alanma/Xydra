package org.xydra.store.impl.delegate;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.MAXDone;
import org.xydra.store.XydraStore;


/**
 * An implementation of {@link XydraStore} with authorisation and access
 * control. Persistence is delegated.
 * 
 * Every component that has a reference to this {@link DelegatingSecureStore}
 * instance has complete access to change everything.
 * 
 * @author xamde
 */
@MAXDone
public class DelegatingSecureStore extends DelegatingStore implements XydraStore {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DelegatingSecureStore.class);
	
	public DelegatingSecureStore(XydraPersistence persistence) {
		super(persistence, new AuthorisationArm(persistence));
	}
	
}
