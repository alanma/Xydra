package org.xydra.store.impl.delegate;

import java.util.HashMap;
import java.util.Map;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.impl.memory.AccountModelWrapper;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.impl.delegate.AccountModelWrapperOnPersistence;
import org.xydra.store.base.HashUtils;


/**
 * Internally, a {@link AccountModelWrapper} is used to use repository as a
 * {@link XGroupDatabase}.
 * 
 * FIXME make thread-safe
 * 
 * @author xamde
 */
public class AuthorisationArm implements XAuthorisationArm {
	
	/**
	 * Maximal number of failed login attempts for this store.
	 */
	public static final int MAX_FAILED_LOGIN_ATTEMPTS = 10;
	
	/**
	 * ActorId used to perform internal changes: {@value}
	 */
	static final XID INTERNAL_XYDRA_ADMIN_ID = XX.toId(NamingUtils.PREFIX_INTERNAL
	        + NamingUtils.NAMESPACE_SEPARATOR + "internalAdmin");
	
	private final XydraPersistence persistence;
	
	private XAccountDatabase accountDb;
	
	private transient Map<XID,XModelArm> modelArms = new HashMap<XID,XModelArm>();
	
	public AuthorisationArm(XydraPersistence persistence) {
		this.persistence = persistence;
		init();
	}
	
	@Override
	public synchronized void init() {
		synchronized(this.modelArms) {
			this.modelArms.clear();
			// set up account model
			XAddress accountModelAddress = X.getIDProvider().fromComponents(
			        this.persistence.getRepositoryId(), NamingUtils.ID_ACCOUNT_MODEL, null, null);
			assert this.persistence.getModelSnapshot(accountModelAddress) == null;
			long result = this.persistence.executeCommand(
			        INTERNAL_XYDRA_ADMIN_ID,
			        X.getCommandFactory().createAddModelCommand(this.persistence.getRepositoryId(),
			                NamingUtils.ID_ACCOUNT_MODEL, true));
			assert result >= 0;
			// set up rights model for account model
			XID rightsForAccountModelId = NamingUtils
			        .getRightsModelId(NamingUtils.ID_ACCOUNT_MODEL);
			long result2 = this.persistence.executeCommand(
			        INTERNAL_XYDRA_ADMIN_ID,
			        X.getCommandFactory().createAddModelCommand(this.persistence.getRepositoryId(),
			                rightsForAccountModelId, true));
			assert result2 >= 0;
			
			// initialise account model wrapper
			AccountModelWrapperOnPersistence groupModelWrapper = new AccountModelWrapperOnPersistence(
			        this.persistence, INTERNAL_XYDRA_ADMIN_ID);
			this.accountDb = groupModelWrapper;
			
			// create initial rights
			/*
			 * Set up a XydraAdmin account, to ensure this account is always
			 * defined. initialise XydraAdmin pass with a long, random string
			 */
			String password = UUID.uuid(100);
			String passwordHash = HashUtils.getXydraPasswordHash(password);
			setXydraAdminPasswordHash(passwordHash);
			
			// TODO use more general convention for admin group name
			XID adminGroup = XX.toId("adminGroup");
			// add to admin group
			this.accountDb.addToGroup(XydraStoreAdmin.XYDRA_ADMIN_ID, adminGroup);
			// FIXME give admin group all rights. @Daniel: how?
		}
		assert this.persistence.getModelSnapshot(X.getIDProvider().fromComponents(
		        this.persistence.getRepositoryId(), NamingUtils.ID_ACCOUNT_MODEL, null, null)) != null;
	}
	
	@Override
	public boolean isAuthorised(XID actorId, String passwordHash) {
		return this.accountDb.isValidLogin(actorId, passwordHash);
	}
	
	@Override
	public XModelArm getModelArm(XID modelId) {
		XID rightsModelId = NamingUtils.getRightsModelId(modelId);
		XModelArm modelArm = this.modelArms.get(rightsModelId);
		if(modelArm == null) {
			// create and put in cache
			modelArm = new ModelArm(this.persistence, modelId, this.getInternalAdminAccount());
			this.modelArms.put(rightsModelId, modelArm);
		}
		return modelArm;
	}
	
	@Override
	public void setXydraAdminPasswordHash(String passwordHash) {
		this.accountDb.setPasswordHash(XydraStoreAdmin.XYDRA_ADMIN_ID, passwordHash);
	}
	
	@Override
	public String getXydraAdminPasswordHash() {
		return this.accountDb.getPasswordHash(XydraStoreAdmin.XYDRA_ADMIN_ID);
	}
	
	@Override
	public void resetFailedLoginAttempts(XID actorId) {
		this.accountDb.resetFailedLoginAttempts(actorId);
	}
	
	@Override
	public int incrementFailedLoginAttempts(XID actorId) {
		return this.accountDb.incrementFailedLoginAttempts(actorId);
	}
	
	@Override
	public int getFailedLoginAttempts(XID actorId) {
		return this.accountDb.getFailedLoginAttempts(actorId);
	}
	
	@Override
	public XID getInternalAdminAccount() {
		return INTERNAL_XYDRA_ADMIN_ID;
	}
	
}
