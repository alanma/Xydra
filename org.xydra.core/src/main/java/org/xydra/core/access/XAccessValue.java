package org.xydra.core.access;

/**
 * XAccessValues are used by {@link XAccessManager XAccessManagers} to define
 * access rights.
 * 
 */

public enum XAccessValue {
	
	ALLOWED, DENIED, UNDEFINED;
	
	/**
	 * Returns true, if the requested access is allowed, false otherwise.
	 * 
	 * @return true, if the requested access is allowed, false otherwise
	 */
	public boolean isAllowed() {
		return this == ALLOWED;
	}
	
	/**
	 * Returns true, if the requested access is denied, false otherwise.
	 * 
	 * @return true, if the requested access is denied, false otherwise
	 */
	public boolean isDenied() {
		return this == DENIED;
	}
	
	/**
	 * Returns true, if the requested access is defined, false otherwise.
	 * 
	 * @return true, if the requested access is defined, false otherwise
	 */
	public boolean isDefined() {
		return this != UNDEFINED;
	}
	
}
