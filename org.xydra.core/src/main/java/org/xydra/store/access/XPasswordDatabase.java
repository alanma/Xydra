package org.xydra.store.access;

import org.xydra.core.model.XID;


/**
 * @author voelkel
 * 
 */
public interface XPasswordDatabase {
	
	/**
	 * @param actorId
	 * @param passwordHash
	 */
	void setPasswordHash(XID actorId, String passwordHash);
	
	/**
	 * @param actorId
	 * @return the stored password hash or null if there is none
	 */
	String getPasswordHash(XID actorId);
	
	/**
	 * @param actorId
	 */
	void removePasswordHash(XID actorId);
	
	/**
	 * @param actorId
	 * @param passwordHash
	 * @return
	 */
	boolean isValidLogin(XID actorId, String passwordHash);
	
	/**
	 * Set number of failed login attempts back to zero.
	 * 
	 * @param actorId
	 */
	void resetFailedLoginAttempts(XID actorId);
	
	/**
	 * Increment number of failed login attempts.
	 * 
	 * @param actorId
	 * @return TODO
	 */
	int incrementFailedLoginAttempts(XID actorId);
	
	/**
	 * @param actorId
	 * @return number of failed loging attempts.
	 */
	int getFailedLoginAttempts(XID actorId);
	
}
