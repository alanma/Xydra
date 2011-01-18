package org.xydra.store.access;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.model.XID;
import org.xydra.store.MAXTodo;


/**
 * @author voelkel
 * 
 */
@MAXTodo
public interface XPasswordDatabase {
	
	/**
	 * Set the passwordHash for the given actorId. From now on, this
	 * passwordHash is valid for authorisation.
	 * 
	 * @param actorId
	 * @param passwordHash
	 */
	@ModificationOperation
	void setPasswordHash(XID actorId, String passwordHash);
	
	/**
	 * @param actorId
	 * @return the stored password hash or null if there is none
	 */
	@ReadOperation
	String getPasswordHash(XID actorId);
	
	/**
	 * Write operation.
	 * 
	 * @param actorId
	 */
	@ModificationOperation
	void removePasswordHash(XID actorId);
	
	/**
	 * @param actorId
	 * @param passwordHash
	 * @return true if the combination (actorId/passwordHash) is a valid login.
	 */
	@ReadOperation
	boolean isValidLogin(XID actorId, String passwordHash);
	
	/**
	 * Set number of failed login attempts back to zero.
	 * 
	 * @param actorId
	 */
	@ModificationOperation
	void resetFailedLoginAttempts(XID actorId);
	
	/**
	 * Increment number of failed login attempts.
	 * 
	 * @param actorId
	 * @return the number of failed login attempts
	 */
	@ModificationOperation
	int incrementFailedLoginAttempts(XID actorId);
	
	/**
	 * @param actorId
	 * @return number of failed loging attempts.
	 */
	@ReadOperation
	int getFailedLoginAttempts(XID actorId);
	
}
