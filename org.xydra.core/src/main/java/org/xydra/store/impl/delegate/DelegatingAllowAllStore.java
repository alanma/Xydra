package org.xydra.store.impl.delegate;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.memory.AllowAllAccessControlManager;


/**
 * An implementation of {@link XydraStore} with no authorisation and access
 * control. Persistence is delegated.
 * 
 * @author xamde
 */

public class DelegatingAllowAllStore extends DelegatingStore implements XydraStore {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DelegatingAllowAllStore.class);
	
	public DelegatingAllowAllStore(XydraPersistence persistence) {
		super(persistence, new AllowAllAccessControlManager());
	}
	
}
