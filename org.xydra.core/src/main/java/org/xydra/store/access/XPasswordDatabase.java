package org.xydra.store.access;

import org.xydra.core.model.XID;
import org.xydra.store.MAXTodo;


/**
 * @author voelkel
 * 
 */
@MAXTodo
public interface XPasswordDatabase {
	
	/**
	 * Write operation.
	 * 
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
	 * Write operation.
	 * 
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
	 * Write operation.
	 * 
	 * Set number of failed login attempts back to zero.
	 * 
	 * @param actorId
	 */
	void resetFailedLoginAttempts(XID actorId);
	
	/**
	 * Write operation.
	 * 
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
