package org.xydra.store.impl.memory;

import org.xydra.base.XID;
import org.xydra.core.XX;
import org.xydra.store.MAXDone;
import org.xydra.store.impl.delegate.XAuthorisationArm;
import org.xydra.store.impl.delegate.XModelArm;


/**
 * Every actorId is authenticated; every actorId may do everything.
 * 
 * @author xamde
 */
@MAXDone
public class AllowAllAuthorisationArm implements XAuthorisationArm {
	
	private static AllowAllModelArm modelArm;
	private String xydraAdminPasswordHash;
	
	@Override
	public boolean isAuthorised(XID actorId, String passwordHash) {
		return true;
	}
	
	@Override
	public synchronized XModelArm getModelArm(XID modelId) {
		if(modelArm == null) {
			modelArm = new AllowAllModelArm();
		}
		return modelArm;
	}
	
	private static class AllowAllModelArm implements XModelArm {
		
		@Override
		public boolean hasModelReadAccess(XID actorId) {
			return true;
		}
		
		@Override
		public boolean hasModelWriteAccess(XID actorId) {
			return true;
		}
		
		@Override
		public boolean hasObjectReadAccess(XID actorId, XID objectId) {
			return true;
		}
		
		@Override
		public boolean hasObjectWriteAccess(XID actorId, XID objectId) {
			return true;
		}
		
		@Override
		public boolean hasFieldReadAccess(XID actorId, XID objectId, XID fieldId) {
			return true;
		}
		
		@Override
		public boolean hasFieldWriteAccess(XID actorId, XID objectId, XID fieldId) {
			return true;
		}
		
	}
	
	@Override
	public void setXydraAdminPasswordHash(String passwordHash) {
		this.xydraAdminPasswordHash = passwordHash;
	}
	
	@Override
	public String getXydraAdminPasswordHash() {
		return this.xydraAdminPasswordHash;
	}
	
	@Override
	public void init() {
		// nothing to do
	}
	
	@Override
	public void resetFailedLoginAttempts(XID actorId) {
	}
	
	@Override
	public int incrementFailedLoginAttempts(XID actorId) {
		return 0;
	}
	
	@Override
	public int getFailedLoginAttempts(XID actorId) {
		return 0;
	}
	
	private static final XID INTERNAL_ADMIN_ACCOUNT_ALLOW_ALL = XX.toId("internal--");
	
	@Override
	public XID getInternalAdminAccount() {
		return INTERNAL_ADMIN_ACCOUNT_ALLOW_ALL;
	}
	
}
