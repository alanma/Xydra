package org.xydra.store.access;

import org.xydra.core.access.XAccessManagerWithListeners;

/**
 * XAccessValues are used by {@link XAccessManagerWithListeners XAccessManagers} to define
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
