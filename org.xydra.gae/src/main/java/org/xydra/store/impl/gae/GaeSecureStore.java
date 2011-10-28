package org.xydra.store.impl.gae;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;
import org.xydra.store.impl.delegate.XydraPersistence;


public class GaeSecureStore extends DelegatingSecureStore {
	
	/**
	 * Default repository ID for new secure stores
	 */
	private static final XID REPO_ID = XX.toId("repo_allow_all");
	
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
