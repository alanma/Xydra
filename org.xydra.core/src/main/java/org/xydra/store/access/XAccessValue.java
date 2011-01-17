package org.xydra.store.access;

/**
 * XAccessValues are used by {@link XAccessDatabase XAccessDatabases} to define
 * access rights.
 */
public enum XAccessValue {
	
	ALLOWED, DENIED, UNDEFINED;
	
	/**
	 * @return true, if the requested access is allowed, false otherwise
	 */
	public boolean isAllowed() {
		return this == ALLOWED;
	}
	
	/**
	 * @return true, if the requested access is denied, false otherwise
	 */
	public boolean isDenied() {
		return this == DENIED;
	}
	
	/**
	 * @return true, if the requested access is defined, false otherwise
	 */
	public boolean isDefined() {
		return this != UNDEFINED;
	}
	
}
