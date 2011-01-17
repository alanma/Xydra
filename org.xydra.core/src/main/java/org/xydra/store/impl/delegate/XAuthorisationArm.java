package org.xydra.store.impl.delegate;

import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.store.MAXDone;
import org.xydra.store.XydraStoreAdmin;


/**
 * Provides authorisation for a complete store/repository. Individual access
 * rights for models are provided via one {@link XModelArm} for each
 * {@link XModel}.
 * 
 * <h2>Security Warning</h2>
 * 
 * Users of this interface must make sure that the internal admin account is not
 * allows to perform <em>any</em> operation from the outside.
 * 
 * @author xamde
 */
@MAXDone
public interface XAuthorisationArm {
	
	/**
	 * @param actorId
	 * @param passwordHash
	 * @return true if the (actorId/passwordHash) combination is a valid account
	 */
	boolean isAuthorised(XID actorId, String passwordHash);
	
	/**
	 * @param modelId
	 * @return an {@link XModelArm} for the given modelId
	 */
	XModelArm getModelArm(XID modelId);
	
	/**
	 * Set the passwordHash for the XydraAdmin account.
	 * 
	 * @param passwordHash
	 */
	void setXydraAdminPasswordHash(String passwordHash);
	
	/**
	 * @return the current passwordHash for the XydraAdmin account
	 */
	String getXydraAdminPasswordHash();
	
	/**
	 * Should initialise internal data in the underlying persistence layer (if
	 * any). This method should be called after a
	 * {@link XydraStoreAdmin#clear()}.
	 */
	void init();
	
	/**
	 * Set the number of failed login attempts to zero.
	 * 
	 * @param actorId
	 */
	void resetFailedLoginAttempts(XID actorId);
	
	/**
	 * @param actorId
	 * @return number of failed login attempts after incrementing
	 */
	int incrementFailedLoginAttempts(XID actorId);
	
	/**
	 * @param actorId
	 * @return number of failed login attempts
	 */
	int getFailedLoginAttempts(XID actorId);
	
	/**
	 * @return XID of <em>internal</em> admin account -- see security warning in
	 *         interface comment
	 */
	XID getInternalAdminAccount();
	
}
