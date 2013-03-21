package org.xydra.store.impl.memory;

import org.xydra.base.XId;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XAuthorisationManager;


/**
 * Every actorId is authenticated; every actorId may do everything.
 * 
 * @author xamde
 */
public class AllowAllAccessControlManager implements XAccessControlManager {
	
	@Override
	public XAuthenticationDatabase getAuthenticationDatabase() {
		return null;
	}
	
	@Override
	public XAuthorisationManager getAuthorisationManager() {
		return null;
	}
	
	@Override
	public void init() {
		// nothing to do
	}
	
	@Override
	public boolean isAuthenticated(XId actorId, String passwordHash) {
		return true;
	}
	
}
