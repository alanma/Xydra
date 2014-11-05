package org.xydra.store.access;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * 
 * There is intentionally no version of this interface with event listeners.
 * There are no internal indexes, all read access can directly got to the data
 * store.
 * 
 * @author xamde
 * 
 */
public interface XAuthenticationDatabase {
	
	/**
	 * Delete all data from this database. Most useful in JUnit tests.
	 */
	@ModificationOperation
	void clear();
	
	/**
	 * @param actorId never null
	 * @return number of failed logging attempts, 0 if not defined.
	 */
	@ReadOperation
	int getFailedLoginAttempts(XId actorId);
	
	/**
	 * @param actorId never null
	 * @return the stored passwordHash or null if there is none. The
	 *         passwordHash is a password encoded via
	 *         {@link HashUtils#getXydraPasswordHash(String)}.
	 */
	@ReadOperation
	String getPasswordHash(XId actorId);
	
	/**
	 * Increment number of failed login attempts.
	 * 
	 * @param actorId never null
	 * @return the number of failed login attempts, 0 if not defined
	 */
	@ModificationOperation
	int incrementFailedLoginAttempts(XId actorId);
	
	/**
	 * Write operation.
	 * 
	 * @param actorId never null
	 */
	@ModificationOperation
	void removePasswordHash(XId actorId);
	
	/**
	 * Set number of failed login attempts back to zero.
	 * 
	 * @param actorId never null
	 */
	@ModificationOperation
	void resetFailedLoginAttempts(XId actorId);
	
	/**
	 * Set the passwordHash for the given actorId. From now on, this
	 * passwordHash is valid for authorisation. The passwordHash is a password
	 * encoded via {@link HashUtils#getXydraPasswordHash(String)}.
	 * 
	 * @param actorId never null
	 * @param passwordHash never null
	 */
	@ModificationOperation
	void setPasswordHash(XId actorId, String passwordHash);
	
}
