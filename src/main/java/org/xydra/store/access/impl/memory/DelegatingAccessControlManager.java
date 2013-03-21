package org.xydra.store.access.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.access.HashUtils;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;


public class DelegatingAccessControlManager implements XAccessControlManager {
	
	protected XAuthenticationDatabase authenticationDb;
	protected XAuthorisationManager authorisationManager;
	
	protected DelegatingAccessControlManager() {
		super();
		this.authorisationManager = null;
		this.authenticationDb = null;
	}
	
	public DelegatingAccessControlManager(XAuthorisationManager authorisationManager,
	        XAuthenticationDatabase authenticationDb) {
		super();
		this.authorisationManager = authorisationManager;
		this.authenticationDb = authenticationDb;
	}
	
	@Override
	public XAuthenticationDatabase getAuthenticationDatabase() {
		return this.authenticationDb;
	}
	
	@Override
	public XAuthorisationManager getAuthorisationManager() {
		return this.authorisationManager;
	}
	
	@Override
	public void init() {
		// create initial rights
		if(this.authenticationDb != null) {
			/*
			 * Initialise XydraAdmin pass with a long, random string. This seals
			 * it from outside access (e.g. via REST).
			 */
			String password = UUID.uuid(100);
			String passwordHash = HashUtils.getXydraPasswordHash(password);
			this.authenticationDb.setPasswordHash(XydraStoreAdmin.XYDRA_ADMIN_ID, passwordHash);
		}
		// else we are some kind of authorize-everybody-store
		
		if(this.authorisationManager != null) {
			/*
			 * FIXME add XydraStoreAmin.XYDRA_ADMIN to built-in administrator
			 * group
			 */
			// TODO @Daniel: use more general convention for admin group name,
			// likely
			// defined in .access package
			XId adminGroup = XX.toId("adminGroup");
			// add to admin group
			XGroupDatabaseWithListeners groupDb = this.authorisationManager.getGroupDatabase();
			if(groupDb != null) {
				groupDb.addToGroup(XydraStoreAdmin.XYDRA_ADMIN_ID, adminGroup);
				// FIXME @Daniel: Admin group should have all rights
			}
			// else we are some kind of group-less-store
			
		}
		// else we are some kind of allow-all-store
	}
	
	@Override
	public boolean isAuthenticated(XId actorId, String passwordHash) {
		if(this.authenticationDb == null) {
			// authenticate EVERY actor with a null password
			return passwordHash == null;
		} else {
			String storedPwHash = this.authenticationDb.getPasswordHash(actorId);
			return storedPwHash != null && storedPwHash.equals(passwordHash);
		}
	}
	
}
