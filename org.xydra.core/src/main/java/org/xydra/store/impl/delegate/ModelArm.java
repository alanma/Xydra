package org.xydra.store.impl.delegate;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.impl.memory.DelegatingAccessManager;
import org.xydra.store.access.XAccessDatabase;


/**
 * Implementation of {@link XModelArm} delegating to an {@link XAccessDatabase}.
 * 
 * @author xamde
 */
public class ModelArm implements XModelArm {
	
	private XID internalAdminAccount;
	
	private DelegatingAccessManager darm;
	
	private XAddress baseModelAddress;
	
	/**
	 * @param persistence
	 * @param modelId base model address
	 * @param internalAdminAccount
	 */
	public ModelArm(XydraPersistence persistence, XID modelId, XID internalAdminAccount) {
		this.internalAdminAccount = internalAdminAccount;
		this.darm = new DelegatingAccessManager(persistence, internalAdminAccount, modelId);
		this.baseModelAddress = X.getIDProvider().fromComponents(persistence.getRepositoryId(),
		        modelId, null, null);
	}
	
	private boolean isInternalAdminAccount(XID actorId) {
		return actorId != null && actorId.equals(this.internalAdminAccount);
	}
	
	@Override
	public boolean hasModelReadAccess(XID actorId) {
		return isInternalAdminAccount(actorId) || this.darm.canRead(actorId, this.baseModelAddress);
	}
	
	@Override
	public boolean hasModelWriteAccess(XID actorId) {
		return isInternalAdminAccount(actorId)
		        || this.darm.canWrite(actorId, this.baseModelAddress);
	}
	
	@Override
	public boolean hasObjectReadAccess(XID actorId, XID objectId) {
		return isInternalAdminAccount(actorId)
		        || this.darm.canRead(actorId, XX.resolveObject(this.baseModelAddress, objectId));
	}
	
	@Override
	public boolean hasObjectWriteAccess(XID actorId, XID objectId) {
		return isInternalAdminAccount(actorId)
		        || this.darm.canWrite(actorId, XX.resolveObject(this.baseModelAddress, objectId));
	}
	
	@Override
	public boolean hasFieldReadAccess(XID actorId, XID objectId, XID fieldId) {
		return isInternalAdminAccount(actorId)
		        || this.darm.canRead(actorId,
		                XX.resolveField(this.baseModelAddress, objectId, fieldId));
	}
	
	@Override
	public boolean hasFieldWriteAccess(XID actorId, XID objectId, XID fieldId) {
		return isInternalAdminAccount(actorId)
		        || this.darm.canWrite(actorId,
		                XX.resolveField(this.baseModelAddress, objectId, fieldId));
	}
	
}
