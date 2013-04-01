package org.xydra.store.impl.gae;

import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;


public class GaeSecureStore extends DelegatingSecureStore {
	
	/**
	 * Default repository ID for new secure stores
	 */
	private static final XId REPO_ID = XX.toId("repo_allow_all");
	
	public GaeSecureStore() {
		super(new GaePersistence(REPO_ID), XydraStoreAdmin.XYDRA_ADMIN_ID);
	}
	
	/**
	 * Create a secure store on top of an existing persistence. Default
	 * repository ID is not used here.
	 * 
	 * @param persistence never null
	 */
	public GaeSecureStore(XydraPersistence persistence) {
		super(persistence, XydraStoreAdmin.XYDRA_ADMIN_ID);
	}
}
